/**
 * Copyright (C) 2011 Samira Tasharofi
 */
package akka.setak.core
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashSet
import akka.actor.UntypedChannel
import akka.setak.Commons._
import akka.actor.ActorRef

case class RealEnvelop(_reciever: UntypedChannel, _message: Any, _sender: UntypedChannel) {
  def receiver = _reciever
  def message = _message
  def sender = _sender

  def ==(otherEnvelop: RealEnvelop): Boolean =
    (receiver == otherEnvelop.receiver) && (sender == otherEnvelop.sender) && (message == otherEnvelop.message)
}
case class FutureMessage(message: Any, senderActor: UntypedChannel)

/**
 * This enumeration defines two different kinds of the events for the
 * messages:
 * delivered(in the mailbox), processed (removed from the mail box and executed)
 *
 * @author <a href="http://www.cs.illinois.edu/homes/tasharo1">Samira Tasharofi</a>
 */
object MessageEventEnum extends Enumeration {
  type MessageEventType = Value
  val Delivered, Processed = Value
}

/**
 * Each test message envelop is a message defined by the user and
 * can be matched with real messages during the execution.
 *
 * The message property in the test message envelop
 * can be an object or a pattern (partial function)
 *
 * The wild card for the sender and receiver is anyActorRef.
 * The wild card for the message is anyMessage.
 *
 * @author <a href="http://www.cs.illinois.edu/homes/tasharo1">Samira Tasharofi</a>
 */
class TestEnvelop {

  var _receiver: UntypedChannel = null
  var _sender: UntypedChannel = null
  var _message: Any = null
  var _messagePattern: PartialFunction[Any, Any] = null

  private def this(sender: UntypedChannel, receiver: UntypedChannel) {
    this()
    this._sender = sender
    this._receiver = receiver
  }

  def this(sender: UntypedChannel, receiver: UntypedChannel, message: Any) {
    this(sender, receiver)
    this._message = message
  }

  def this(sender: UntypedChannel, receiver: UntypedChannel, messagePattern: PartialFunction[Any, Any]) {
    this(sender, receiver)
    this._messagePattern = messagePattern
  }

  def receiver = _receiver

  def sender = _sender

  def message = _message

  def messagePattern = _messagePattern

  def matchWithRealEnvelop(realEnvelop: RealEnvelop): Boolean = {
    log("matching " + this.toString() + " " + realEnvelop.toString())
    if (!compareChannels(this.sender, realEnvelop.sender)) return false

    if (!compareChannels(this.receiver, realEnvelop.receiver)) return false

    if (this.message != null && this.message != anyMessage && this.message != realEnvelop.message) return false

    if (this.messagePattern != null && !this.messagePattern.isDefinedAt(realEnvelop.message)) return false
    log(" returns true")
    return true
  }

  private def compareChannels(ch1: UntypedChannel, ch2: UntypedChannel): Boolean = {
    (ch1 == anyActorRef) || (ch2 == anyActorRef) || (ch1 == ch2)
  }

  /**
   * This operator can be applied to test messages to create a sequence(order) of the test messages which can be used for
   * deterministic execution via "setScheudle" API.
   */
  /*  def ->(testEnvelop: TestEnvelop): TestSchedule = {
    return (new TestSchedule(this) -> testEnvelop)
  }
*/
  override def toString(): String = "(" + sender + "," + receiver + "," + (if (message != null) message else messagePattern) + ")"

  //only for debugging
  private var debug = false
  private def log(s: String) = if (debug) println(s)

}

/**
 * Each test message envelop sequence is an ordered set of test message
 * envelops. The sequence is defined by using '->' operator.
 * The assumption is that the sender of all messages in a given sequence are the same.
 *
 * @author <a href="http://www.cs.illinois.edu/homes/tasharo1">Samira Tasharofi</a>
 */
class TestEnvelopSequence(testEnvelops: HashSet[TestEnvelop]) {

  protected var _envelopSequence = ListBuffer[HashSet[TestEnvelop]](testEnvelops)

  def ->(testEnvelops: TestEnvelop*): TestEnvelopSequence = {
    var envelopSet = HashSet[TestEnvelop]()
    for (envelop ← testEnvelops)
      envelopSet += (envelop)
    _envelopSequence.+=(envelopSet)
    return this
  }
  def ->(testEnvelops: HashSet[TestEnvelop]): TestEnvelopSequence = {
    var envelopSet = HashSet[TestEnvelop]()
    for (envelop ← testEnvelops)
      envelopSet += (envelop)
    _envelopSequence.+=(envelopSet)
    return this
  }

  def heads: HashSet[TestEnvelop] = _envelopSequence.headOption.orNull
  def tails: HashSet[TestEnvelop] = _envelopSequence.reverse.headOption.orNull

  //  def removeMatchingFromHead(realEnvelop: RealEnvelop): TestEnvelop = {
  //    if (!_envelopSequence.isEmpty) {
  //      for (envelop ← heads) {
  //        if (envelop.matchWithRealEnvelop(realEnvelop)) {
  //          heads.-=(envelop)
  //          if (heads.isEmpty)
  //            _envelopSequence.-=(heads)
  //          return envelop
  //        }
  //      }
  //    }
  //    return null
  //  }

  def removeFromHead(testEnvelop: TestEnvelop): Boolean = {
    if (!_envelopSequence.isEmpty) {
      for (envelop ← heads) {
        if (envelop == testEnvelop) {
          heads.-=(envelop)
          if (heads.isEmpty)
            _envelopSequence.-=(heads)
          return true
        }
      }
    }
    return false
  }

  //def envelopSequence = _envelopSequence

  def isEmpty: Boolean = {
    return _envelopSequence.isEmpty
  }

  def matchingIndexOf(realEnvelop: RealEnvelop): Int = {
    val matchingTestEnvelop = getMatchingTestEnvelop(realEnvelop)
    if (matchingTestEnvelop != null) return indexOf(matchingTestEnvelop)
    else return -1
    //      var index = -1
    //      for (envelopSet ← _envelopSequence) {
    //        index += 1
    //        for (envelop ← envelopSet) {
    //          if (envelop.matchWithRealEnvelop(realEnvelop))
    //            return index
    //        }
    //  
    //      }
    //      return -1
  }

  def indexOf(testEnvelop: TestEnvelop): Int = {
    var index = -1
    for (envelopSet ← _envelopSequence) {
      index += 1
      for (envelop ← envelopSet) {
        if (envelop == testEnvelop)
          return index
      }

    }
    return -1
  }

  def getMatchingTestEnvelop(realEnvelop: RealEnvelop): TestEnvelop = {
    for (envelopSet ← _envelopSequence) {
      for (envelop ← envelopSet) {
        if (envelop.matchWithRealEnvelop(realEnvelop))
          return envelop
      }

    }
    return null
  }

  //  def equals(otherSequence: TestSchedule): Boolean = {
  //    for (envelop ← _envelopSequence) {
  //      if (!otherSequence.head.equals(msg)) return false
  //    }
  //    return true
  //  }

  override def toString() = {
    var result = ""
    for (envelopSet ← _envelopSequence) {
      result += "("
      for (envelop ← envelopSet) {
        result += envelop.toString() + ","
      }
      result += ") -> "
    }
    result
  }

}

object TestEnvelopSequence {
  //implicit def toSequence(testEnvelop: TestEnvelop): TestEnvelopSequence = new TestEnvelopSequence(testEnvelop)
  implicit def toSequence(testEnvelops: TestEnvelop*): TestEnvelopSequence = {
    var envelopSet = new HashSet[TestEnvelop]
    for (testEnvelop ← testEnvelops) envelopSet.add(testEnvelop)
    new TestEnvelopSequence(envelopSet)
  }
  implicit def toSequence(testEnvelops: Set[TestEnvelop]): TestEnvelopSequence = {
    var envelopSet = new HashSet[TestEnvelop]
    for (testEnvelop ← testEnvelops) envelopSet.add(testEnvelop)
    new TestEnvelopSequence(envelopSet)
  }
  implicit def toSequence(testEnvelop: TestEnvelop): TestEnvelopSequence = {
    var envelopSet = HashSet(testEnvelop)
    new TestEnvelopSequence(envelopSet)
  }
}
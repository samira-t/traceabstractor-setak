/**
 * Copyright (C) 2011 Samira Tasharofi
 */
package akka.setak.util
import akka.setak.core.TestActorRef
import scala.collection.mutable.ListBuffer
import akka.setak.core.monitor._
import akka.setak.core.TestEnvelop
import akka.actor.UntypedChannel
import akka.setak.core.TestSchedule
import akka.setak.core.TestSchedule
import scala.collection.mutable.HashSet
import akka.setak.core.MessageEventEnum
import scala.collection.mutable.ArrayBuffer
import akka.actor.ActorRef

/**
 * @author <a href="http://www.cs.illinois.edu/homes/tasharo1">Samira Tasharofi</a>
 */
class TestEnvelopUtil(implicit traceMonitorActor: ActorRef) {

  /**
   * Factories for creating test message envelops
   * Each test message envelop is identified by sender, receiver and
   * message. The message parameter can be an object or a partial function
   * for pattern matching.
   *
   * Each defined test message is also added to the set of test messages
   * in the Monitor object so that it can set the set of test messages that
   * should be traced by the testMonitorActor.
   */
  def testEnvelop(sender: UntypedChannel, receiver: UntypedChannel, message: Any): TestEnvelop = {
    var msg = new TestEnvelop(sender, receiver, message)
    (traceMonitorActor ? AddTestEnvelop(msg)).get
    msg
  }

  def testEnvelopPattern(sender: UntypedChannel, receiver: UntypedChannel, envelopPattern: PartialFunction[Any, Any]): TestEnvelop = {
    var msg = new TestEnvelop(sender, receiver, envelopPattern)
    (traceMonitorActor ? AddTestEnvelop(msg)).get
    msg
  }

  val anyMessage = new Object()

  /**
   * Checks if the message is delivered or not by asking from trace monitor actor.
   */
  def isDelivered(testEnvelop: TestEnvelop): Boolean = {
    (traceMonitorActor ? MatchedMessageEventCount(testEnvelop, MessageEventEnum.Delivered)).mapTo[Int].get > 0
  }

  /**
   * @return the number of the test messages delivered.
   */
  def deliveryCount(testEnvelop: TestEnvelop): Int = {
    (traceMonitorActor ? MatchedMessageEventCount(testEnvelop, MessageEventEnum.Delivered)).mapTo[Int].get
  }

  /**
   * Checks if the message is processed by asking from trace monitor actor.
   */
  def isProcessed(testEnvelop: TestEnvelop): Boolean = {
    (traceMonitorActor ? MatchedMessageEventCount(testEnvelop, MessageEventEnum.Processed)).mapTo[Int].get > 0
  }

  /**
   * @return the number of the test messages processed.
   */
  def processingCount(testEnvelop: TestEnvelop): Int = {
    (traceMonitorActor ? MatchedMessageEventCount(testEnvelop, MessageEventEnum.Processed)).mapTo[Int].get
  }

}


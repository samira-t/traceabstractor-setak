/**
 * Copyright (C) 2011 Samira Tasharofi
 */
package akka.setak.core.monitor
import akka.actor.Actor
import scala.collection.mutable.ListBuffer
import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import akka.setak.core.TestEnvelop
import akka.setak.core.RealEnvelop
import akka.setak.core.MessageEventEnum._
import akka.actor.LocalActorRef
import akka.actor.UntypedChannel

abstract class MonitorActorMessage
case class AsyncMessageEvent(message: RealEnvelop, event: MessageEventType) extends MonitorActorMessage
case class ReplyMessageEvent(message: RealEnvelop) extends MonitorActorMessage
case class MatchedMessageEventCount(testEnvelop: TestEnvelop, event: MessageEventType) extends MonitorActorMessage
case class AddTestEnvelop(testEnvelop: TestEnvelop) extends MonitorActorMessage
case object AllDeliveredMessagesAreProcessed extends MonitorActorMessage
case object AllTestMessagesAreProcessed extends MonitorActorMessage
case class NotifyMeForMessageEvent(testEnvelops: Set[TestEnvelop], event: MessageEventType) extends MonitorActorMessage
case object NotProcessedMessages extends MonitorActorMessage
case object ClearState extends MonitorActorMessage

/**
 * This actor manages the monitoring of the test execution.
 * It contains the set of test messages defined by the user
 * and uses this set to match with the messages sent/received by TestActorRef
 * For the efficiency, the actor just keeps track of the messages that match
 * with the user defined test messages.
 * It can report which test messages delivered/processed and how many times
 * they are delivered/processed.
 *
 * @author <a href="http://www.cs.illinois.edu/homes/tasharo1">Samira Tasharofi</a>
 */

class TraceMonitorActor() extends Actor {

  var testEnvelopsInfo = new HashMap[TestEnvelop, Array[Int]]()
  var deliveredAsyncMessages = new ArrayBuffer[RealEnvelop]()
  var messageTrace = new ListBuffer[TestEnvelop]
  var listener: Listener = null

  def receive =
    {
      case AddTestEnvelop(testEnvelop) ⇒ {
        testEnvelopsInfo.put(testEnvelop, Array(0, 0))
        self.reply()
      }
      case AsyncMessageEvent(message, event) ⇒ {
        val matchedTestEnvelops = testEnvelopsInfo.filterKeys(m ⇒ m.matchWithRealEnvelop(message))
        for ((testMsg, dp) ← matchedTestEnvelops) {
          event match {
            case Delivered ⇒ {
              deliveredAsyncMessages.+=(message)
              testEnvelopsInfo.update(testMsg, Array(dp(0) + 1, dp(1)))
            }
            case Processed ⇒ {
              testEnvelopsInfo.update(testMsg, Array(dp(0), dp(1) + 1))
              val index = deliveredAsyncMessages.indexWhere(m ⇒ m == message)
              if (index >= 0) deliveredAsyncMessages.remove(index)
              if (message.message.equals("Reply")) log("reply" + index + " " + testEnvelopsInfo(testMsg)(1))
              log("received processing: " + message.message + " " + message.receiver)
            }

          }
        }
        if (matchedTestEnvelops.size > 0 && listener != null) {
          notifyListener()
        }
      }
      case ReplyMessageEvent(message) ⇒ {
        //        messageTrace.+=(message)
        val matchedTestEnvelops = testEnvelopsInfo.filterKeys(m ⇒ m.matchWithRealEnvelop(message))
        for ((testMsg, dp) ← matchedTestEnvelops) {
          testEnvelopsInfo.update(testMsg, Array(dp(0) + 1, dp(1) + 1))
        }

      }

      case NotifyMeForMessageEvent(testEnvelops, event) ⇒ {
        listener = Listener(self.channel, testEnvelops, event)
        notifyListener()
      }
      /**
       * returns the set of the real  messages that are matched with the test message and the specified event
       */
      case MatchedMessageEventCount(testEnvelop, event) ⇒ {
        event match {
          case Delivered ⇒ self.reply(testEnvelopsInfo(testEnvelop)(0))
          case Processed ⇒ {
            self.reply(testEnvelopsInfo(testEnvelop)(1))
            //log(testEnvelop.message + " " + testEnvelopsInfo(testEnvelop)(1) + " " + testEnvelopsInfo(testEnvelop)(0))
          }
        }
      }
      case AllDeliveredMessagesAreProcessed ⇒ self.reply(deliveredAsyncMessages.size == 0)
      case NotProcessedMessages             ⇒ self.reply(deliveredAsyncMessages)

      case ClearState ⇒ {
        testEnvelopsInfo = new HashMap[TestEnvelop, Array[Int]]()
        deliveredAsyncMessages = new ArrayBuffer[RealEnvelop]()
        messageTrace = new ListBuffer[TestEnvelop]
        self.reply()

      }

    }

  private def notifyListener() {
    listener.event match {
      case Processed ⇒ {
        if ((listener.testEnvelops != null && allListenerTestMessagesAreProcessed) ||
          (listener.testEnvelops == null && allTestMessagesAreProcessed)) {
          listener.channel ! true
          listener = null

        }
      }
      case Delivered ⇒ {
        if ((listener.testEnvelops != null && allListenerTestMessagesAreDelivered) ||
          (listener.testEnvelops == null && allTestMessagesAreDelivered)) {
          listener.channel ! true
          listener = null

        }

      }
    }

  }

  private def allTestMessagesAreProcessed(): Boolean = {
    for ((message, Array(deliverCount, processCount)) ← testEnvelopsInfo) {
      if (processCount == 0) {
        return false
      }
    }
    return true
  }
  private def allListenerTestMessagesAreProcessed(): Boolean = {
    for (message ← listener.testEnvelops) {
      if (testEnvelopsInfo(message)(1) == 0) {

        return false
      }
    }
    return true
  }
  private def allListenerTestMessagesAreDelivered(): Boolean = {
    for (message ← listener.testEnvelops) {
      if (testEnvelopsInfo(message)(0) == 0) {
        return false
      }
    }
    return true
  }

  private def allTestMessagesAreDelivered(): Boolean = {
    for ((message, Array(deliverCount, processCount)) ← testEnvelopsInfo) {
      if (deliverCount == 0) return false
    }
    return true
  }

  //for debugging only
  private var debug = false
  private def log(s: String) = if (debug) println(s)

}
case class Listener(channel: UntypedChannel, testEnvelops: Set[TestEnvelop], event: MessageEventType)
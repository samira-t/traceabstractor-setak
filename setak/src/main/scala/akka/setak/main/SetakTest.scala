/**
 * Copyright (C) 2011 Samira Tasharofi
 */
package akka.setak
import core.TestExecutionManager
import core.monitor.TraceMonitorActor
import util.TestEnvelopUtil
import core.TestEnvelop
import core.TestActorRef
import util.TestActorRefFactory
import core.TestSchedule
import akka.actor.Actor
import akka.japi.Creator
import akka.actor.UntypedChannel
import akka.setak.core.TestEnvelopSequence
import scala.collection.mutable.HashSet

trait SetakTest {

  implicit var traceMonitorActor = akka.actor.Actor.actorOf[TraceMonitorActor].start()
  implicit var testExecutionManager = new TestExecutionManager()
  implicit var testEnvelopUtil = new TestEnvelopUtil()
  implicit var anonymousSchedule: TestSchedule = null
  implicit var testActorRefFactory = new TestActorRefFactory()
  Commons.testFactoryPool.add(testActorRefFactory)
  var testInitialized = true

  def superBeforeEach() {
    if (!testInitialized) {
      traceMonitorActor = akka.actor.Actor.actorOf[TraceMonitorActor].start()
      testExecutionManager = new TestExecutionManager()
      testEnvelopUtil = new TestEnvelopUtil()
      testActorRefFactory = new TestActorRefFactory()
      Commons.testFactoryPool.add(testActorRefFactory)
      anonymousSchedule = null
      testInitialized = true
    }
  }

  def superAfterEach() {
    testExecutionManager.stopTest
    Commons.testFactoryPool.remove(this)
    testInitialized = false
  }

  /**
   * Waits for the system to gets stable and then executes the body which is usually a set of
   * assertion statements.
   */
  def whenStable(body: ⇒ Unit)(implicit tryCount: Int = TestConfig.maxTryForStability) = {
    val isStable = testExecutionManager.checkForStability(tryCount)
    if (isStable) {
      body
    } else throw new Exception("The system didn't get stable")
  }

  /**
   * Waits for a test message to be processed and then executes the body which is usually a set of
   * assertion statements.
   */
  def afterMessages(testEnvelops: TestEnvelop*)(body: ⇒ Unit) {
    val processed = testExecutionManager.waitForMessage(testEnvelops.toSet[TestEnvelop])
    if (processed) {
      body
    } else throw new Exception("The message didn't get processed")
  }

  /**
   * Waits for all test messages to be processed and then executes the body which is usually a set of
   * assertion statements.
   */
  def afterAllMessages(body: ⇒ Unit) {
    val processed = testExecutionManager.waitForAllMessages()
    if (processed) {
      body
    } else throw new Exception("Some of the messages didn't get processed")
  }
  /**
   * After the timeout, executes the body that usually contains some assertions.
   */
  def afterTimeOut(timeout: Long)(body: ⇒ Unit) = {
    Thread.sleep(timeout)
    body
  }

  /*
   * testEnvelopUtil API calls
   */
  def testEnvelop(sender: UntypedChannel, receiver: UntypedChannel, message: Any) =
    testEnvelopUtil.testEnvelop(sender, receiver, message)

  def testEnvelopPattern(sender: UntypedChannel, receiver: UntypedChannel, envelopPattern: PartialFunction[Any, Any]) =
    testEnvelopUtil.testEnvelopPattern(sender, receiver, envelopPattern)

  /**
   * Checks if the message is delivered or not by asking from trace monitor actor.
   */
  def isDelivered(testEnvelop: TestEnvelop) = testEnvelopUtil.isDelivered(testEnvelop)

  /**
   * @return the number of the test messages delivered.
   */
  def deliveryCount(testEnvelop: TestEnvelop) = testEnvelopUtil.deliveryCount(testEnvelop)

  /**
   * Checks if the message is processed by asking from trace monitor actor.
   */
  def isProcessed(testEnvelop: TestEnvelop) = testEnvelopUtil.isProcessed(testEnvelop)

  /**
   * @return the number of the test messages processed.
   */
  def processingCount(testEnvelop: TestEnvelop) = testEnvelopUtil.processingCount(testEnvelop)

  /**
   * API for constraining the schedule of test execution and removing some non-determinism by specifying
   * a set of partial orders between the messages. The receivers of the messages in each partial order should
   * be the same (an instance of TestActorRef)
   */
  def setSchedule(partialOrders: TestEnvelopSequence*) {
    /*
     * TODO: check if the receivers of all messages in each partial order are the same
     */
    for (partialOrder ← partialOrders) {
      val receiver = partialOrder.heads.first._receiver
      if (receiver.isInstanceOf[TestActorRef]) {
        receiver.asInstanceOf[TestActorRef].addPartialOrderToSchedule(partialOrder)
      } else if (receiver == Commons.anyActorRef) {
        if (anonymousSchedule == null) anonymousSchedule = new TestSchedule
        anonymousSchedule.addPartialOrder(partialOrder)
      } else {
        throw new Exception("The receiver of the test message in a schedule should be anyActorRef or an instance of TestActorRef")
      }
    }
  }

  def setSchedule(partialOrders: HashSet[TestEnvelopSequence]) {
    /*
     * TODO: check if the receivers of all messages in each partial order are the same
     */
    for (partialOrder ← partialOrders) {
      val receiver = partialOrder.heads.first._receiver
      if (receiver.isInstanceOf[TestActorRef]) {
        receiver.asInstanceOf[TestActorRef].addPartialOrderToSchedule(partialOrder)
      } else if (receiver == Commons.anyActorRef) {
        if (anonymousSchedule == null) anonymousSchedule = new TestSchedule
        anonymousSchedule.addPartialOrder(partialOrder)
      } else {
        throw new Exception("The receiver of the test message in a schedule should be anyActorRef or an instance of TestActorRef")
      }
    }
  }

  /*
   * testActorRefFactory API calls
   */
  def actorOf[T <: Actor: Manifest] = testActorRefFactory.actorOf[T]

  def actorOf[T <: Actor](clazz: Class[T]) = testActorRefFactory.actorOf(clazz)

  def actorOf[T <: Actor](factory: ⇒ T) = testActorRefFactory.actorOf(factory)

}
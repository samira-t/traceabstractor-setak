/**
 * Copyright (C) 2011 Samira Tasharofi
 */
package akka.setak.test

import akka.setak._

import org.junit.Test
import org.junit.Before
import org.junit.After
import akka.setak.core.TestEnvelop
import akka.setak.core.TestActorRef
import akka.setak.Commons._
import akka.actor.Actor
import scala.collection.mutable.HashSet

case class IntMessage(x: Int)
case class BooleanMessage(b: Boolean)

class IntBoolActor extends Actor {
  var intMessages = new HashSet[Int]
  var boolMessages = new HashSet[Boolean]
  def receive = {
    case IntMessage(x)     ⇒ intMessages.add(x)
    case BooleanMessage(b) ⇒ boolMessages.add(b)
  }
}

class JUnitTestEnvelopMatching extends SetakJUnit {

  var a: TestActorRef = null
  var intAny: TestEnvelop = null
  var int1: TestEnvelop = null
  var btrue: TestEnvelop = null
  var bAny: TestEnvelop = null

  @Before
  def setUp {
    a = actorOf(new IntBoolActor()).start
    int1 = testEnvelop(anyActorRef, a, IntMessage(1))
    intAny = testEnvelopPattern(anyActorRef, a, { case IntMessage(_) ⇒ })
    btrue = testEnvelop(anyActorRef, a, BooleanMessage(true))
    bAny = testEnvelopPattern(anyActorRef, a, { case BooleanMessage(_) ⇒ })
  }

  @Test
  def test1 {
    a ! IntMessage(5)
    a ! BooleanMessage(false)
    a ! BooleanMessage(false)
    a ! IntMessage(1)

    afterMessages(int1) {
      assert(isDelivered(int1))
      assert(isProcessed(intAny))
      assert(deliveryCount(intAny) == 2)
      assert(processingCount(bAny) == 2)
    }
  }

}

class ScalaTestEnvelopMatching extends SetakFlatSpec with org.scalatest.matchers.ShouldMatchers {

  var testActor: TestActorRef = null
  var intAny: TestEnvelop = null
  var int1: TestEnvelop = null
  var btrue: TestEnvelop = null
  var bAny: TestEnvelop = null

  override def setUp {
    testActor = actorOf[IntBoolActor].start
    int1 = testEnvelop(anyActorRef, testActor, IntMessage(1))
    intAny = testEnvelopPattern(anyActorRef, testActor, { case IntMessage(_) ⇒ })
    btrue = testEnvelop(anyActorRef, testActor, BooleanMessage(true))
    bAny = testEnvelopPattern(anyActorRef, testActor, { case BooleanMessage(_) ⇒ })
  }

  "The Int and Bool messages" should "be processed" in {
    testActor ! IntMessage(5)
    testActor ! BooleanMessage(false)
    testActor ! BooleanMessage(false)
    testActor ! IntMessage(1)

    afterMessages(int1, bAny) {
      isDelivered(int1) should be(true)
      isProcessed(intAny) should be(true)
      deliveryCount(intAny) should be(2)
      processingCount(bAny) should be(2)
    }

  }

}
/**
 * Copyright (C) 2011 Samira Tasharofi
 */
package akka.setak.test
import akka.actor.Actor
import akka.actor.ActorRef
import org.junit.Test
import org.junit.Before
import org.junit.After
import akka.setak.core.TestEnvelop
import akka.setak.core.TestActorRef
import akka.setak.core.TestEnvelopSequence._
import akka.setak.Commons._
import akka.setak._
import scala.collection.mutable.ListBuffer
import akka.setak.SetakJUnit

class SampleActor(var brother: ActorRef = null) extends Actor {
  var messageOrder = ListBuffer[Any]()
  def receive = {
    case msg @ ('m)  ⇒ messageOrder.+=(msg)
    case msg @ 'req  ⇒ messageOrder.+=(msg); if (brother != null) { val f = brother ? 'req2; f.get }
    case msg @ 'req2 ⇒ messageOrder.+=(msg); self.reply('reply)

  }
}

class TestFutureMeesages extends SetakJUnit {

  var a: TestActorRef = null
  var b: TestActorRef = null
  var m: TestEnvelop = null
  var req2: TestEnvelop = null
  var reply: TestEnvelop = null

  @Before
  def setUp {
    a = actorOf(new SampleActor()).start
    b = actorOf(new SampleActor(a)).start
    m = testEnvelop(anyActorRef, a, 'm)
    req2 = testEnvelop(anyActorRef, a, 'req2)
    reply = testEnvelop(a, anyActorRef, 'reply)
  }

  @Test
  def testDeliveryToFuture {
    b ! 'req

    whenStable {
      assert(isProcessed(req2))
      assert(isDelivered(reply))
    }

  }

  @Test
  def testScheduleFutureAndActorMessages {
    setSchedule(req2 -> m)
    a ! 'm
    b ! 'req

    whenStable {
      assert(isProcessed(req2))
      assert(isDelivered(reply))
      assert(a.actorObject[SampleActor].messageOrder.indexOf('req2) < a.actorObject[SampleActor].messageOrder.indexOf('m))
    }
  }

  @After
  def tearDown {
  }

}
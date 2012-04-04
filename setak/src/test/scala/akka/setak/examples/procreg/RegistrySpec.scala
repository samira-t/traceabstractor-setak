package akka.setak.examples.procreg

import akka.actor.Actor
import akka.actor.ActorRef
import org.junit.Test
import org.junit.Before
import org.junit.After
import akka.setak.core.TestEnvelop
import akka.setak.core.TestEnvelopSequence._
import akka.setak.core.TestActorRef
import akka.setak.SetakJUnit
import akka.setak.SetakFlatSpec
import akka.setak.Commons._
import akka.setak.SetakTest

class RegistrySpec extends SetakFlatSpec {

  var server: ActorRef = _
  var ets: ActorRef = _
  var runTime: ActorRef = _

  var client1: ActorRef = _
  var client2: ActorRef = _
  var client3: ActorRef = _

  //  "a simple test" should "pass" in {
  //
  //    runTime = actorOf[RunTime].start
  //    ets = actorOf[ETS].start
  //    server = actorOf(new RegistryServer(ets, runTime)).start
  //
  //    client1 = actorOf(new Client(server, runTime, ets)).start
  //    client2 = actorOf(new Client(server, runTime, ets)).start
  //    client3 = actorOf(new Client(server, runTime, ets)).start
  //
  //    val reg1 = testEnvelopPattern(anyActorRef, client1, { case Register(_, _) ⇒ })
  //    val reg2 = testEnvelopPattern(anyActorRef, client2, { case Register(_, _) ⇒ })
  //
  //    val pid = (runTime ? spawn("pName")).mapTo[Int].get
  //    (runTime ? kill(pid)).get
  //
  //    client1 ! Register("pName", pid)
  //    client2 ! Register("pName", pid)
  //
  //    afterAllMessages {
  //      println("finished")
  //      
  //    }
  //
  //  }

  "a test" should "fail" in {

    runTime = actorOf[RunTime].start
    ets = actorOf[ETS].start
    server = actorOf(new RegistryServer(ets, runTime)).start

    client1 = actorOf(new Client(server, runTime, ets)).start
    client2 = actorOf(new Client(server, runTime, ets)).start

    val insert1 = testEnvelopPattern(anyActorRef, ets, { case InsertNewForward(_, _) ⇒ })
    val insert2 = testEnvelopPattern(anyActorRef, ets, { case InsertNewForward(_, _) ⇒ })

    val reg1 = testEnvelopPattern(anyActorRef, client1, { case Register(_, _) ⇒ })
    val reg2 = testEnvelopPattern(anyActorRef, client2, { case Register(_, _) ⇒ })

    setSchedule(insert1 -> insert2)

    val pid = (runTime ? spawn("pName")).mapTo[Int].get
    (runTime ? kill(pid)).get

    client1 ! Register("pName", pid)
    client2 ! Register("pName", pid)

    afterAllMessages {
      println("finished")
    }

  }

  //  @org.junit.Test
  //  def testBugScenario1 {
  //    val backwardRegister = createScheduleMessagePattern(ANY_ACTOR, RegistryServer, { case Reg(_) => () })
  //    val audit = createScheduleMessagePattern(ANY_ACTOR, RegistryServer, { case Audit(_) => () })
  //    setSchedule(audit -> backwardRegister)
  //    val pid = (RunTime !? spawn("pName")).asInstanceOf[Int]
  //    RunTime !? kill(pid)
  //
  //    client1 ! Register("pName", pid)
  //    client2 ! Register("pName", pid)
  //    assertWhenStable("Exception is thrown in at least one client", client2.exceptionIsThrown || client1.exceptionIsThrown)
  //  }

  //  @org.junit.Test  
  //  def testWithoutBug {
  //    val backwardRegister1 = createScheduleMessagePattern(client1, RegistryServer, {case Reg(_) => ()})
  //    val backwardRegister2 = createScheduleMessagePattern(client2, RegistryServer, {case Reg(_) => ()})
  //    val forwardRegister3 = createScheduleMessagePattern(client3, ETS, {case InsertNewForward(_, _) => () })
  //    val down = createScheduleMessagePattern(ANY_ACTOR, RegistryServer, {case Down(_) => ()})
  //    setSchedule(backwardRegister1 -> backwardRegister2 -> down -> forwardRegister3)
  //    val pid = RunTime.spawn("pName")
  //    RunTime.kill(pid)
  //    client1 ! Register("pName", pid)
  //    client2 ! Register("pName", pid)
  //    client3 ! Register("pName", pid)
  //    assertWhenStable("No exception is thrown in in any client", !client1.exceptionIsThrown && !client2.exceptionIsThrown && !client3.exceptionIsThrown)
  //  }
  //
  //  @org.junit.Test  
  //  def testBugScenario2 {
  //    val backwardRegister1 = createScheduleMessagePattern(client1, RegistryServer, {case Reg(_) => ()})
  //    val forwardRegister2 = createMultipleScheduleMessagePattern(2,client2, ETS, {case InsertNewForward(_, _) => ()})
  //    val backwardRegister2 = createScheduleMessagePattern(client2, RegistryServer, {case Reg(_) => ()})
  //    val audit3 = createScheduleMessagePattern(client3, RegistryServer, {case Audit(_) => ()})
  //    val down = createScheduleMessagePattern(ANY_ACTOR, RegistryServer, {case Down(_) => ()})
  //    setSchedule(backwardRegister1 -> forwardRegister2(0)-> forwardRegister2(1) -> audit3 -> backwardRegister2)
  //    val pid = RunTime.spawn("pName")
  //    RunTime.kill(pid)
  //    client1 ! Register("pName", pid)
  //    client2 ! Register("pName", pid)
  //    client3 ! Register("pName", pid)
  //    assertWhenStable("Exception is thrown in at least one client", client2.exceptionIsThrown || client3.exceptionIsThrown)
  //    setSchedule(down)
  //  }
}

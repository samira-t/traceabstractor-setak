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
import akka.setak.ScheduleFileReader
import akka.setak.ScheduleGenerator

class RegistrySpec extends SetakFlatSpec {

  implicit def test = this

  var server: TestActorRef = _
  var ets: TestActorRef = _
  var runTime: TestActorRef = _

  var client1: TestActorRef = _
  var client2: TestActorRef = _
  var client3: TestActorRef = _

  override def setUp() {
    val traceFile = scala.io.Source.fromFile("./src/test/scala/akka/setak/examples/procreg/procreg-orig.txt")
    reader = new ScheduleFileReader(traceFile)

  }

  private def setUpTest() {
    this.superBeforeEach()
    runTime = actorOf[RunTime].start
    ets = actorOf[ETS].start
    server = actorOf(new RegistryServer(ets, runTime)).start

    client1 = actorOf(new Client(server, runTime, ets)).start
    client2 = actorOf(new Client(server, runTime, ets)).start

    val mapper = new RegistryMapper()

    scheduleGenerator = new ScheduleGenerator(mapper)

  }

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

  private def setTraceFileReader(original: Boolean) {
    var traceFileName = if (original) ("./src/test/scala/akka/setak/examples/procreg/procreg-orig.txt") else
      ("./src/test/scala/akka/setak/examples/procreg/procreg-red.txt")
    reader = new ScheduleFileReader(scala.io.Source.fromFile(traceFileName))

  }

  "a test of reduced" should "fail" in {
    println("a test of reduced shoudl fail")
    setTraceFileReader(false)

    val schedulesLinesMap = reader.getSchedulesLines("EXCEPTION")
    var counter = 0
    for (key ← schedulesLinesMap.keySet) {
      setUpTest()
      var scheduleLines = schedulesLinesMap.get(key).get
      println(key)
      counter += 1
      println("counter = " + counter)
      val schedule = scheduleGenerator.getSchedule(scheduleLines)
      //println("line sheudles=" + scheduleLines)
      setSchedule(schedule)
      //println("line Number=" + key)

      val pid = (runTime ? spawn("pName")).mapTo[Int].get
      (runTime ? kill(pid)).get

      client1 ! Register("pName", pid)
      client2 ! Register("pName", pid)

      afterAllMessages {
        assert(client1.actorObject[Client].exceptionIsThrown || client2.actorObject[Client].exceptionIsThrown)
      }
      tearDownTest()
    }
  }

  "a test of reduced" should "pass" in {
    println("a test of reduced should pass")
    setTraceFileReader(false)

    val schedulesLinesMap = reader.getSchedulesLines("NO_BUG")
    var counter = 0
    for (key ← schedulesLinesMap.keySet) {
      setUpTest()
      var scheduleLines = schedulesLinesMap.get(key).get
      println(key)
      counter += 1
      println("counter = " + counter)
      val schedule = scheduleGenerator.getSchedule(scheduleLines)
      //println("line sheudles=" + scheduleLines)
      setSchedule(schedule)
      //println("line Number=" + key)

      val pid = (runTime ? spawn("pName")).mapTo[Int].get
      (runTime ? kill(pid)).get

      client1 ! Register("pName", pid)
      client2 ! Register("pName", pid)

      afterAllMessages {
        assert(!client1.actorObject[Client].exceptionIsThrown && !client2.actorObject[Client].exceptionIsThrown)
      }
      tearDownTest()
    }
  }

  "a test of original" should "fail" in {
    println("a test of original should fail")
    setTraceFileReader(true)

    val schedulesLinesMap = reader.getSchedulesLines("EXCEPTION")
    var counter = 0
    for (key ← schedulesLinesMap.keySet) {
      setUpTest()
      var scheduleLines = schedulesLinesMap.get(key).get
      println(key)
      counter += 1
      println("counter = " + counter)
      val schedule = scheduleGenerator.getSchedule(scheduleLines)
      println("line sheudles=" + scheduleLines)
      setSchedule(schedule)
      println("line Number=" + key)

      val pid = (runTime ? spawn("pName")).mapTo[Int].get
      (runTime ? kill(pid)).get

      client1 ! Register("pName", pid)
      client2 ! Register("pName", pid)
      println("line Number=" + key)

      afterAllMessages {
        assert(client1.actorObject[Client].exceptionIsThrown || client2.actorObject[Client].exceptionIsThrown)
      }
      tearDownTest()
    }
  }

  "a test of original" should "pass" in {
    println("a test of original should pass")

    setTraceFileReader(true)

    val schedulesLinesMap = reader.getSchedulesLines("NO_BUG")
    var counter = 0
    for (key ← schedulesLinesMap.keySet) {
      setUpTest()
      var scheduleLines = schedulesLinesMap.get(key).get
      println(key)
      counter += 1
      println("counter = " + counter)
      val schedule = scheduleGenerator.getSchedule(scheduleLines)
      //println("line sheudles=" + scheduleLines)
      setSchedule(schedule)
      //println("line Number=" + key)

      val pid = (runTime ? spawn("pName")).mapTo[Int].get
      (runTime ? kill(pid)).get

      client1 ! Register("pName", pid)
      client2 ! Register("pName", pid)

      afterAllMessages {
        assert(!client1.actorObject[Client].exceptionIsThrown && !client2.actorObject[Client].exceptionIsThrown)
      }
      tearDownTest()
    }
  }
  private def tearDownTest() {
    server.stop()
    ets.stop()
    runTime.stop()
    client1.stop()
    client2.stop()

    this.superAfterEach()
  }

  //    val insert1 = testEnvelopPattern(client1, ets, { case InsertNewForward(_, _) ⇒ })
  //    val insert2 = testEnvelopPattern(client2, ets, { case InsertNewForward(_, _) ⇒ })
  //    //val backward = testEnvelopPattern(server, ets, { case InsertNewBackward(_, _) ⇒ })
  //
  //    val reg1 = testEnvelopPattern(anyActorRef, client1, { case Register(_, _) ⇒ })
  //    val reg2 = testEnvelopPattern(anyActorRef, client2, { case Register(_, _) ⇒ })
  //
  //    setSchedule(insert2 -> insert1)
  //
  //    val pid = (runTime ? spawn("pName")).mapTo[Int].get
  //    (runTime ? kill(pid)).get
  //
  //    client1 ! Register("pName", pid)
  //    client2 ! Register("pName", pid)
  //
  //    afterAllMessages {
  //      println("finished")
  //    }

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

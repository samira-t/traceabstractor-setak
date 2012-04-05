package akka.setak.examples.sleepingbarber

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
import test.scala.akka.setak.ScheduleFileReader
import test.scala.akka.setak.ScheduleGenerator

//
//
//import scala.actors.Actor._
//import scala.actors.Actor
//
//
///**
// * @author Samira Tasharofi
// */
//
//object SleepingBarberTest{
//  def main(args:Array[String]): Unit = {
//  var capacity = 4
//  var customerNum = capacity
//  var customers = new Array[Customer](customerNum)
//    WaitingRoom.capacity = capacity
//    for (i <- 0 to capacity-1)
//      customers(i) = new Customer("customer" + i, WaitingRoom)
//
//    Barber.start
//    WaitingRoom.start
//    customers.foreach(c => {c.start(); Thread.sleep(10)})
//    
//  }
//}

class BarberSpec extends SetakFlatSpec {

  implicit def test = this

  var capacity = 4
  var customerNum = capacity
  var customers = new Array[TestActorRef](customerNum)
  var waitingRoom: TestActorRef = _
  var barber: TestActorRef = _

  var scheduleGenerator: ScheduleGenerator = _
  var reader: ScheduleFileReader = _

  //  //messages to waiting room
  //  var enterc1w: TestEnvelop = _
  //  var enterc2w: TestEnvelop = _
  //  var enterc3w: TestEnvelop = _
  //  var enterc4w: TestEnvelop = _
  //  var nextbw1: TestEnvelop = _
  //  var nextbw2: TestEnvelop = _
  //  var nextbw3: TestEnvelop = _
  //  var nextbw4: TestEnvelop = _
  //  var nextww: TestEnvelop = _
  //
  //  //messages to barber
  //  var enterwb1: TestEnvelop = _
  //  var enterwb2: TestEnvelop = _
  //  var enterwb3: TestEnvelop = _
  //  var enterwb4: TestEnvelop = _
  //  var waitwb: TestEnvelop = _
  //
  //  //messages to c1
  //  var startc1: TestEnvelop = _
  //  var donec1: TestEnvelop = _
  //
  //  //messages to c2
  //  var startc2: TestEnvelop = _
  //  var donec2: TestEnvelop = _
  //  var waitc2: TestEnvelop = _
  //
  //  //messages to c3
  //  var startc3: TestEnvelop = _
  //  var donec3: TestEnvelop = _
  //  var waitc3: TestEnvelop = _
  //
  //  //messages to c4
  //  var startc4: TestEnvelop = _
  //  var donec4: TestEnvelop = _
  //  var waitc4: TestEnvelop = _
  //  var fullc4: TestEnvelop = _

  override def setUp() {
    val traceFile = scala.io.Source.fromFile("./src/test/scala/akka/setak/examples/sleepingbarber/sleepingbarber.txt")
    reader = new ScheduleFileReader(traceFile)

    //    enterc1w = testEnvelopPattern(customers(0), waitingRoom, { case Enter(_) ⇒ })
    //    enterc2w = testEnvelopPattern(customers(1), waitingRoom, { case Enter(_) ⇒ })
    //    enterc3w = testEnvelopPattern(customers(2), waitingRoom, { case Enter(_) ⇒ })
    //    enterc4w = testEnvelopPattern(customers(3), waitingRoom, { case Enter(_) ⇒ })
    //    nextbw1 = testEnvelop(barber, waitingRoom, Next)
    //    nextbw2 = testEnvelop(barber, waitingRoom, Next)
    //    nextbw3 = testEnvelop(barber, waitingRoom, Next)
    //    nextww = testEnvelop(waitingRoom, waitingRoom, Next)
    //
    //    enterwb1 = testEnvelopPattern(waitingRoom, barber, { case Enter(_) ⇒ })
    //    enterwb2 = testEnvelopPattern(waitingRoom, barber, { case Enter(_) ⇒ })
    //    enterwb3 = testEnvelopPattern(waitingRoom, barber, { case Enter(_) ⇒ })
    //    waitwb = testEnvelop(waitingRoom, barber, Wait)
    //
    //    donec1 = testEnvelop(barber, customers(0), Done)
    //
    //    startc2 = testEnvelop(barber, customers(1), Start)
    //    waitc2 = testEnvelop(waitingRoom, customers(1), Wait)
    //    donec2 = testEnvelop(barber, customers(1), Done)
    //
    //    waitc3 = testEnvelop(waitingRoom, customers(2), Wait)
    //    donec3 = testEnvelop(barber, customers(2), Done)

  }

  private def setUpTest() {
    this.superBeforeEach()
    barber = actorOf[Barber]
    waitingRoom = actorOf(new WaitingRoom(capacity, barber))
    for (i ← 0 to capacity - 1)
      customers(i) = actorOf(new Customer("customer" + i, waitingRoom))

    val mapper = new BarberMapper()

    scheduleGenerator = new ScheduleGenerator(mapper)

    //ScheduleGenerator.test = this.asInstanceOf[BarberSpec]

  }

  "test reading schedules from file" should "pass" in {

    val schedulesLines = reader.getSchedulesLines("NO_BUG")
    var counter = 0
    for (scheduleLines ← schedulesLines) {
      counter += 1
      println("counter = " + counter)
      setUpTest()
      val schedule = scheduleGenerator.getSchedule(scheduleLines)
      setSchedule(schedule)
      barber.start
      waitingRoom.start
      customers.foreach(c ⇒ c.start())
      afterAllMessages {
        for (customer ← customers) assert(customer.actorObject[Customer].state != CustomerState.NotServed)
      }
      tearDownTest()
    }

  }

  private def tearDownTest() {

    barber.stop
    waitingRoom.stop
    customers.foreach(c ⇒ c.stop())
    this.superAfterEach()
  }

  "test reading schedules from file" should "fail" in {

    val schedulesLines = reader.getSchedulesLines("EXCEPTION")
    var counter = 0
    for (scheduleLines ← schedulesLines) {
      counter += 1
      println("counter = " + counter)
      setUpTest()
      val schedule = scheduleGenerator.getSchedule(scheduleLines)
      setSchedule(schedule)
      barber.start
      waitingRoom.start
      customers.foreach(c ⇒ c.start())
      afterAllMessages {

        var oneIsNotServed = false
        for (customer ← customers) {
          println(customer.actorObject[Customer].state)
          if (customer.actorObject[Customer].state == CustomerState.NotServed)
            oneIsNotServed = true
        }
        assert(oneIsNotServed)

      }
      tearDownTest()

    }
  }
}

/*  "schedule1" should "reveals no bug (all of them should not be served" in {

    enterwb4 = testEnvelopPattern(waitingRoom, barber, { case Enter(_) ⇒ })

    nextbw4 = testEnvelop(barber, waitingRoom, Next)

    startc1 = testEnvelop(barber, customers(0), Start)

    startc4 = testEnvelop(barber, customers(3), Start)
    waitc4 = testEnvelop(waitingRoom, customers(3), Wait)
    donec4 = testEnvelop(barber, customers(3), Done)

    setSchedule(startc1 -> donec1,
      enterc1w -> (nextww, enterc2w, enterc3w, enterc4w) -> nextbw1 -> nextbw2 -> nextbw3 -> nextbw4,
      enterwb1 -> enterwb2 -> enterwb3 -> enterwb4 -> waitwb,
      waitc2 -> startc2 -> donec2,
      waitc3 -> donec3,
      waitc4 -> startc4 -> donec4)

    barber.start
    waitingRoom.start
    customers.foreach(c ⇒ c.start())

    afterAllMessages {
      for (customer ← customers) assert(customer.actorObject[Customer].state != CustomerState.NotServed)
    }

  }

  "schedule2" should "reveals the bug (one of them should not be served" in {
    fullc4 = testEnvelop(waitingRoom, customers(3), Full)

    startc3 = testEnvelop(barber, customers(2), Start)

    setSchedule(donec1,
      enterc1w -> (enterc2w, enterc3w) -> enterc4w -> nextww -> nextbw1 -> nextbw2 -> nextbw3,
      enterwb1 -> enterwb2 -> enterwb3 -> waitwb,
      waitc2 -> startc2 -> donec2,
      Set(waitc3, startc3) -> donec3,
      fullc4)

    barber.start
    waitingRoom.start
    customers.foreach(c ⇒ c.start())

    afterAllMessages {

      var oneIsNotServed = false
      for (customer ← customers) {
        println(customer.actorObject[Customer].state)
        if (customer.actorObject[Customer].state == CustomerState.NotServed)
          oneIsNotServed = true
      }
      assert(oneIsNotServed)
    }

  }
*/

//NO_BUG
//Reduction in number of schedules=3, out of 18 schedules 
//Number of removed constraints =26 
//---------------------------------------------
//Schedule: 1
//3:
//(1,3,Start,)->(1,3,Done,)
//2:
//(3,2,Enter(sleepingbarber.Customer@bb05),1)->(4,2,Enter(sleepingbarber.Customer@bb10),)->(5,2,Enter(sleepingbarber.Customer@bb2a),),(2,2,Next,)->(6,2,Enter(sleepingbarber.Customer@bb3c),)->(1,2,Next,)->(1,2,Next,8)->(1,2,Next,12)->(1,2,Next,16)
//1:
//(2,1,Enter(sleepingbarber.Customer@bb2a),)->(2,1,Enter(sleepingbarber.Customer@bb3c),12)->(2,1,Enter(sleepingbarber.Customer@bb10),14)->(2,1,Enter(sleepingbarber.Customer@bb05),16)->(2,1,Wait,)
//6:
//(2,6,Wait,),(1,6,Start,)->(1,6,Done,)
//5:
//(2,5,Wait,)->(1,5,Done,)
//4:
//(2,4,Wait,)->(1,4,Start,)->(1,4,Done,)
//
//---------------------------------------------
//Schedule: 2
//3:
//(1,3,Start,)->(1,3,Done,)
//2:
//(3,2,Enter(sleepingbarber.Customer@bb05),1)->(4,2,Enter(sleepingbarber.Customer@bb10),)->(5,2,Enter(sleepingbarber.Customer@bb2a),),(2,2,Next,)->(6,2,Enter(sleepingbarber.Customer@bb3c),)->(1,2,Next,)->(1,2,Next,8)->(1,2,Next,12)->(1,2,Next,16)
//1:
//(2,1,Enter(sleepingbarber.Customer@bb2a),)->(2,1,Enter(sleepingbarber.Customer@bb3c),12)->(2,1,Enter(sleepingbarber.Customer@bb10),14)->(2,1,Enter(sleepingbarber.Customer@bb05),16)->(2,1,Wait,)
//6:
//(2,6,Wait,)->(1,6,Start,)->(1,6,Done,)
//5:
//(2,5,Wait,)->(1,5,Done,)
//4:
//(1,4,Start,),(2,4,Wait,)->(1,4,Done,)
//
//---------------------------------------------
//Schedule: 3
//3:
//(1,3,Start,)->(1,3,Done,)
//2:
//(3,2,Enter(sleepingbarber.Customer@bb05),1)->(4,2,Enter(sleepingbarber.Customer@bb10),),(6,2,Enter(sleepingbarber.Customer@bb3c),),(2,2,Next,),(5,2,Enter(sleepingbarber.Customer@bb2a),)->(1,2,Next,)->(1,2,Next,8)->(1,2,Next,12)->(1,2,Next,16)
//1:
//(2,1,Enter(sleepingbarber.Customer@bb3c),)->(2,1,Enter(sleepingbarber.Customer@bb2a),12)->(2,1,Enter(sleepingbarber.Customer@bb10),14)->(2,1,Enter(sleepingbarber.Customer@bb05),16)->(2,1,Wait,)
//6:
//(2,6,Wait,)->(1,6,Start,)->(1,6,Done,)
//5:
//(2,5,Wait,)->(1,5,Done,)
//4:
//(2,4,Wait,)->(1,4,Start,)->(1,4,Done,)

/*===========================
Reduction in number of schedules=3, out of 7 schedules 
Number of removed constraints =10 
---------------------------------------------
Schedule: 1
3:
(1,3,Done,)
2:
(3,2,Enter(sleepingbarber.Customer@bb05),1)->(4,2,Enter(sleepingbarber.Customer@bb10),)->(5,2,Enter(sleepingbarber.Customer@bb2a),)->(6,2,Enter(sleepingbarber.Customer@bb3c),)->(2,2,Next,)->(1,2,Next,)->(1,2,Next,8)->(1,2,Next,12)
1:
(2,1,Enter(sleepingbarber.Customer@bb2a),)->(2,1,Enter(sleepingbarber.Customer@bb10),12)->(2,1,Enter(sleepingbarber.Customer@bb05),14)->(2,1,Wait,)
6:
(2,6,Full,)
5:
(2,5,Wait,),(1,5,Start,)->(1,5,Done,)
4:
(2,4,Wait,),(1,4,Start,)->(1,4,Done,)

---------------------------------------------
Schedule: 2
3:
(1,3,Done,)
2:
(3,2,Enter(sleepingbarber.Customer@bb05),1)->(5,2,Enter(sleepingbarber.Customer@bb2a),),(4,2,Enter(sleepingbarber.Customer@bb10),)->(6,2,Enter(sleepingbarber.Customer@bb3c),)->(2,2,Next,)->(1,2,Next,)->(1,2,Next,8)->(1,2,Next,12)
1:
(2,1,Enter(sleepingbarber.Customer@bb10),)->(2,1,Enter(sleepingbarber.Customer@bb2a),12)->(2,1,Enter(sleepingbarber.Customer@bb05),14)->(2,1,Wait,)
6:
(2,6,Full,)
5:
(2,5,Wait,),(1,5,Start,)->(1,5,Done,)
4:
(2,4,Wait,)->(1,4,Start,)->(1,4,Done,)

---------------------------------------------
Schedule: 3
3:
(1,3,Done,)
2:
(3,2,Enter(sleepingbarber.Customer@bb05),1)->(5,2,Enter(sleepingbarber.Customer@bb2a),),(4,2,Enter(sleepingbarber.Customer@bb10),)->(6,2,Enter(sleepingbarber.Customer@bb3c),)->(2,2,Next,)->(1,2,Next,)->(1,2,Next,8)->(1,2,Next,12)
1:
(2,1,Enter(sleepingbarber.Customer@bb10),)->(2,1,Enter(sleepingbarber.Customer@bb2a),12)->(2,1,Enter(sleepingbarber.Customer@bb05),14)->(2,1,Wait,)
6:
(2,6,Full,)
5:
(2,5,Wait,)->(1,5,Start,)->(1,5,Done,)
4:
(1,4,Start,),(2,4,Wait,)->(1,4,Done,)

*/ 
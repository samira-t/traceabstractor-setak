//package akka.setak.examples.sleepingbarber
//
//import akka.setak.core.TestEnvelop
//import akka.setak.core.TestEnvelopSequence._
//import akka.setak.core.TestActorRef
//import scala.collection.mutable.HashSet
//import akka.setak.core.TestSchedule
//import akka.setak.Commons._
//import scala.collection.mutable.ArrayBuffer
//import akka.setak.core.TestEnvelopSequence
//import akka.setak.SetakTest
//import test.scala.akka.setak.EnvelopMapper
//
//class BarberMapper(implicit test: BarberSpec) extends EnvelopMapper {
//
//  //  var traceFile: scala.io.BufferedSource = _
//  //
//  //  var test: BarberSpec = null
//  //
//  //  def getSchedule(lines: HashSet[String])(implicit test: SetakTest) = {
//  //    var pos = new HashSet[TestEnvelopSequence]()
//  //
//  //    var curLine = ""
//  //    for (line ← lines) {
//  //      curLine = line.replace("(_)", "_");
//  //      var sequence: TestEnvelopSequence = null
//  //      var recStr = ""
//  //      while (curLine.indexOf("(") >= 0) {
//  //        val event = curLine.substring(curLine.indexOf("(") + 1, curLine.indexOf(")") - 1)
//  //        //println("event " + event)
//  //        val eventParts = event.split(",")
//  //        recStr = eventParts(1)
//  //        val sender = getActorRef(eventParts(0))
//  //        val receiver = getActorRef(eventParts(1))
//  //        var envelop: TestEnvelop = null
//  //        if (eventParts(2).contains("_")) {
//  //          envelop = test.testEnvelopPattern(sender, receiver, getPFContent(eventParts(2)))
//  //        } else {
//  //          envelop = test.testEnvelop(sender, receiver, getContent(eventParts(2)))
//  //        }
//  //        //if (recStr.equals("2")) println(curLine)
//  //        if (sequence == null) sequence = envelop
//  //        else {
//  //          if (curLine.startsWith("->")) {
//  //            sequence ->= envelop
//  //            //if (recStr.equals("2")) println(sequence)
//  //          } else if (curLine.startsWith(",(")) {
//  //            sequence.tails.add(envelop)
//  //            //if (recStr.equals("2")) println(sequence)
//  //          } else println("error")
//  //        }
//  //        curLine = curLine.substring(curLine.indexOf(")") + 1)
//  //
//  //      }
//  //      //if (recStr.equals("2")) println(sequence)
//  //      pos.add(sequence)
//  //
//  //    }
//  //    pos
//  //  }
//
//  def getActorRef(refStr: String) = {
//    refStr match {
//      case "1" ⇒ test.barber
//      case "2" ⇒ test.waitingRoom
//      case _ ⇒ {
//        val index = refStr.toInt - 3
//        test.customers(index)
//      }
//    }
//
//  }
//
//  def getContent(contentStr: String) = {
//    contentStr match {
//      case "Next"  ⇒ Next
//      case "Wait"  ⇒ Wait
//      case "Done"  ⇒ Done
//      case "Start" ⇒ Start
//      case "Full"  ⇒ Full
//    }
//  }
//
//  def getPFContent(contentStr: String): PartialFunction[Any, Any] = {
//    contentStr match {
//      case "Enter_" ⇒ { case Enter(_) ⇒ }
//
//    }
//  }
//
//}
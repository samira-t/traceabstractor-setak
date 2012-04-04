//package akka.setak.examples.procreg
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
//class RegistryMapper(implicit test: RegistrySpec) extends EnvelopMapper {
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
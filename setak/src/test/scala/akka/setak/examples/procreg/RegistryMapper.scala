package akka.setak.examples.procreg

import akka.setak.core.TestEnvelop
import akka.setak.core.TestEnvelopSequence._
import akka.setak.core.TestActorRef
import scala.collection.mutable.HashSet
import akka.setak.core.TestSchedule
import akka.setak.Commons._
import scala.collection.mutable.ArrayBuffer
import akka.setak.core.TestEnvelopSequence
import akka.setak.SetakTest
import akka.setak.EnvelopMapper
import akka.actor.ActorRef

class RegistryMapper(implicit test: RegistrySpec) extends EnvelopMapper {

  def getActorRef(refStr: String) = {
    refStr match {
      case "1" ⇒ test.client1
      case "2" ⇒ test.client2
      case "3" ⇒ test.client3
      case "4" ⇒ null //test.runTime
      case "7" ⇒ test.server
      case "6" ⇒ test.ets
      case "8" ⇒ anyActorRef
      case _   ⇒ null

    }

  }

  def getContent(contentStr: String) = {
    contentStr match {
      case "resetState" ⇒ resetState
    }
  }

  def getPFContent(contentStr: String): PartialFunction[Any, Any] = {
    contentStr match {
      case "Reg_"               ⇒ { case Reg(_) ⇒ }
      case "Where_"             ⇒ { case Where(name: String) ⇒ }
      case "Unreg_"             ⇒ { case Unreg(name: String) ⇒ }
      case "Send_"              ⇒ { case Send(name: String, msg: String) ⇒ }
      case "Audit_"             ⇒ { case Audit(name: String) ⇒ }
      case "Down_"              ⇒ { case Down(pid: Int) ⇒ }
      case "Register_"          ⇒ { case Register(name: String, pid: Int) ⇒ }
      case "Spawn_"             ⇒ { case Spawn(name: String) ⇒ }
      case "Kill_"              ⇒ { case Kill(pid: Int) ⇒ }
      case "InsertNewForward_"  ⇒ { case InsertNewForward(name: String, pid: Int) ⇒ }
      case "InsertNewBackward_" ⇒ { case InsertNewBackward(pid: Int, monitor: ActorRef) ⇒ }
      case "Lookup_"            ⇒ { case Lookup(name: String) ⇒ }
      case "GetMatch_"          ⇒ { case GetMatch(pid: Int) ⇒ }
      case "DeleteEntry_"       ⇒ { case DeleteEntry(pid: Int) ⇒ }
      case "spawn_"             ⇒ { case spawn(name: String) ⇒ }
      case "kill_"              ⇒ { case kill(pid: Int) ⇒ }
      case "isProcessAlive_"    ⇒ { case isProcessAlive(pid: Int) ⇒ }
    }

  }
}
package akka.setak.examples.procreg

import akka.actor._
import akka.actor.Actor._
import java.util.Random
import akka.setak.core.TestActorRef
import scala.collection.mutable.{ HashMap, HashSet }

/**
 * @author Samira Tasharofi
 *
 * (Translated from Erlang)
 * http://portal.acm.org/citation.cfm?id=1596574
 */

case class Reg(pid: Int)
case class Where(name: String)
case class Unreg(name: String)
case class Send(name: String, msg: String)
case class Audit(name: String)
case class Down(pid: Int)

class RegistryServer(ets: ActorRef, runTime: ActorRef) extends Actor {
  def receive() = {
    case Reg(pid: Int) ⇒ {
      val monitor = new Monitor(pid, runTime, self)
      val result = (ets ? InsertNewBackward(pid, monitor)).get
    }
    case Audit(name) ⇒ {

      val pid = (ets ? Lookup(name)).mapTo[Int].get
      val alive = (runTime ? isProcessAlive(pid)).mapTo[Boolean].get
      if (!alive) {
        (ets ? DeleteEntry(pid)).get
      }
      self.reply()
    }
    case Down(pid: Int) ⇒ {
      if ((ets ? GetMatch(pid)).get != None)
        (ets ? DeleteEntry(pid)).get
    }
    case Unreg(name) ⇒ {
      val pid = (ets ? Lookup(name)).mapTo[Int].get
      (ets ? DeleteEntry(pid)).get
      self.reply()
    }
    case Where(name) ⇒ {
      val pid = (ets ? Lookup(name)).mapTo[Int].get
      if ((runTime ? isProcessAlive(pid)).mapTo[Boolean].get) self.reply(pid)
      else self.reply(-1)
    }
  }
}

case class Register(name: String, pid: Int)
case class Spawn(name: String)
case class Kill(pid: Int)

class Client(server: ActorRef, runTime: ActorRef, ets: ActorRef) extends Actor {
  var pid = -1
  var name = ""
  var exceptionIsThrown = false
  def receive() = {
    case Spawn(name)         ⇒ self.reply((runTime ? spawn(name)).get)
    case Kill(pid)           ⇒ (runTime ? kill(pid)).get
    case Register(name, pid) ⇒ println("register"); reg(name, pid); self.stop
  }

  def reg(name: String, pid: Int) {
    var result = (ets ? (InsertNewForward(name, pid))).mapTo[Boolean].get
    if (!result) {
      var pidInTable = (server ? Where(name)).mapTo[Int].get
      if (pidInTable == -1) {
        (server ? Audit(name)).get
        result = (ets ? (InsertNewForward(name, pid))).mapTo[Boolean].get
        if (!result) {
          pidInTable = (server ? Where(name)).mapTo[Int].get
          if (!result && pidInTable == -1) {
            exceptionIsThrown = true
            println("*************************************88Exception")
            //self.stop
          }
        } else {
          server ! Reg(pid)
        }
      }
    } else {
      server ! Reg(pid)
    }
  }
}

class Monitor(pid: Int, runTime: ActorRef, server: ActorRef) {
  private var alive = (runTime ? isProcessAlive(pid)).mapTo[Boolean].get
  while (alive) {
    Thread.sleep(1000)
    alive = (runTime ? isProcessAlive(pid)).mapTo[Boolean].get
  }
  server ! Down(pid)
}

case class InsertNewForward(name: String, pid: Int)
case class InsertNewBackward(pid: Int, monitor: Monitor)
case class Lookup(name: String)
case class GetMatch(pid: Int)
case class DeleteEntry(pid: Int)

class ETS extends Actor {
  var forwardTable = HashMap[String, Int]()
  var backwardTable = HashMap[Int, Monitor]()
  def receive() = {
    case InsertNewForward(name, pid) ⇒ {
      println("insert forward")
      if (forwardTable.contains(name)) {
        self.reply(false)
      } else {
        forwardTable.+=((name, pid))
        self.reply(true)
      }
    }
    case InsertNewBackward(pid, monitor) ⇒ {
      if (backwardTable.contains(pid)) {
        self.reply(false)
      } else {
        backwardTable.+=((pid, monitor))
        self.reply(true)
      }
    }
    case Lookup(name) ⇒ {
      forwardTable.get(name) match {
        case None      ⇒ self.reply(-1)
        case Some(pid) ⇒ self.reply(pid)
      }
    }
    case GetMatch(pid) ⇒ {
      self.reply(backwardTable.get(pid))
    }
    case DeleteEntry(pid) ⇒ {
      backwardTable.get(pid) match {
        case None ⇒ self.reply()
        case Some(monitor) ⇒ {
          var name = ""
          val forwardElements = forwardTable.elements
          while (forwardElements.hasNext && name.equals("")) {
            val elem = forwardElements.next
            if (elem._2 == pid)
              name = elem._1
          }
          forwardTable.-=(name)
          backwardTable.-=(pid)
          self.reply()
        }
      }
    }
  }
}

case class spawn(name: String)
case class kill(pid: Int)
case class isProcessAlive(pid: Int)
case object resetState

class RunTime extends Actor {
  private var currentID = 0
  private var alive = HashSet[Int]()
  private var dead = HashSet[Int]()
  def receive() = {
    case spawn(name: String) ⇒ {
      currentID += 1
      alive.add(currentID)
      self.reply(currentID)
    }
    case kill(pid: Int) ⇒ {
      dead.add(pid)
      alive.remove(pid)
      self.reply()
    }

    case isProcessAlive(pid: Int) ⇒ {
      self.reply(alive.contains(pid))
    }

    case resetState ⇒ {
      currentID = 0
      alive.clear()
      dead.clear()
      self.reply()
    }
  }
}

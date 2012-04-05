package akka.setak.examples.sleepingbarber

import akka.actor._
import akka.actor.Actor._
import java.util.Random
import akka.setak.core.TestActorRef

/**
 * @author Samira Tasharofi
 *
 * (Translated from Groovy)
 * http://code.google.com/p/gparallelizer/source/browse/trunk/src/test/groovy/groovyx/gpars/samples/actors/DemoSleepingBarber.groovy?r=1125
 */

case class Enter(customer: ActorRef)
case object Next
case object Full
case object Wait
case object Start
case object Done

class Barber extends Actor {
  val random = new Random()

  def receive = {
    case Enter(customer) ⇒ {
      customer ! Start
      doTheHaircut(random)
      customer ! Done
      self.reply(Next)
    }
    case Wait ⇒ {
      // No customer
      //self.reply(Next)
    }
  }

  private def doTheHaircut(random: Random) {
    //Thread.sleep(random.nextInt(10) * 100)
  }
}

object CustomerState extends Enumeration {
  val Entered, NotServed, Waiting, BeingServed, Served, Exception = Value
}

class Customer(name: String, waitingRoom: ActorRef, number: Int = 0) extends Actor {
  import CustomerState._
  var state: CustomerState.Value = _

  override def preStart() {
    //waitingRoom ! Enter(self)
    //    if (number == 0)
    //      waitingRoom ! Enter(self.asInstanceOf[TestActorRef].actorOf(new Customer(name, waitingRoom, number + 1)).start)
    //    else
    waitingRoom ! Enter(self)
    state = Entered
  }

  def receive() = {
    case Full ⇒ {
      state = NotServed
      //println(name + "Full")
      //throw new Exception("should not receive full")
      //self.stop()

    }
    case Wait ⇒ {
      state = Waiting
      //println(name + "Wait")
    }
    case Start ⇒ {
      state = BeingServed
      //println(name + "Start")
      //self.stop()
    }
    case Done ⇒ {
      state = Served
      //println("exit")
      //exit()
      //println(name + "Done")
      //self.stop
    }
  }
}

class WaitingRoom(capacity: Int, barber: ActorRef) extends Actor {
  private var waitingCustomers = List[ActorRef]()
  private var barberWaiting = true

  def receive() = {
    case Enter(customer) ⇒ {
      //bug: it should be "if (waitingCustomers.length == capacity) {"
      if (waitingCustomers.length == (capacity - 1)) {
        self.reply(Full)
      } else {
        waitingCustomers ::= (customer)
        //waitingCustomers = waitingCustomers.reverse
        if (barberWaiting) {
          barberWaiting = false
          self ! Next
        } else {
          self.reply(Wait)
          /**
           * cannot be detected
           */
          /*self ! Next*/
        }
      }
    }
    case Next ⇒ {
      if (waitingCustomers.length > 0) {
        //if (waitingCustomers.length > 1) {
        val customer = waitingCustomers(0)
        waitingCustomers = waitingCustomers.drop(1)
        barber ! Enter(customer)
        //barber ! Enter(waitingCustomers(1))
      } else {
        barberWaiting = true
        barber ! Wait
      }
    }
  }
}

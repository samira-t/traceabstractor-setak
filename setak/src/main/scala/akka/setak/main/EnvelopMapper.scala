package akka.setak
import akka.setak.core.TestActorRef
import akka.actor.ActorRef

trait EnvelopMapper {

  def getActorRef(refStr: String): ActorRef

  def getContent(contentStr: String): Any

  def getPFContent(contentStr: String): PartialFunction[Any, Any]

}
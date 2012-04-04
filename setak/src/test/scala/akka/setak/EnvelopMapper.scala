package test.scala.akka.setak
import akka.setak.core.TestActorRef

trait EnvelopMapper {

  def getActorRef(refStr: String): TestActorRef

  def getContent(contentStr: String): Any

  def getPFContent(contentStr: String): PartialFunction[Any, Any]

}
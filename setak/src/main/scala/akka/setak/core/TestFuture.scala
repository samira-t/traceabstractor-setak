//package akka.setak.core
//import akka.dispatch.MessageDispatcher
//import scala.actors.threadpool.TimeUnit
//
//case class TestFuture(timeout: Long, timeunit: TimeUnit)(implicit dispatcher: MessageDispatcher, val implicit actor:TestActorRef) 
//extends ActorCompletableFuture(timeout: Long, timeunit: TimeUnit)(implicit dispatcher: MessageDispatcher)//{
////  def this()(implicit dispatcher: MessageDispatcher) = this(0, MILLIS)
////  def this(timeout: Long)(implicit dispatcher: MessageDispatcher) = this(timeout, MILLIS)
////
////  def !(message: Any)(implicit channel: UntypedChannel) = completeWithResult(message)
////
////  override def sendException(ex: Throwable) = {
////    completeWithException(ex)
////    value == Some(Left(ex))
////  }
////
////  def channel: UntypedChannel = this
////
////  @deprecated("ActorCompletableFuture merged with Channel[Any], just use 'this'", "1.2")
////  def future = this
////}
////
////object ActorCompletableFuture {
////  def apply(f: CompletableFuture[Any]): ActorCompletableFuture =
////    new ActorCompletableFuture(f.timeoutInNanos, NANOS)(f.dispatcher) {
////      completeWith(f)
////      override def !(message: Any)(implicit channel: UntypedChannel) = f completeWithResult message
////      override def sendException(ex: Throwable) = {
////        f completeWithException ex
////        f.value == Some(Left(ex))
////      }
////    }
////}

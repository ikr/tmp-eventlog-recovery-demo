package erd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Clock {
  sealed trait Message
  private case object Tick                                     extends Message
  private final case class Echo(text: String)                  extends Message
  private final case class ObservationFailure(message: String) extends Message

  def apply(observer: ActorRef[MemorizingEcho.Signal]): Behavior[Message] = setup(observer)

  private def setup(observer: ActorRef[MemorizingEcho.Signal]): Behavior[Message] = Behaviors.withTimers { timers =>
    timers.startTimerWithFixedDelay(Tick, 10.seconds)
    receive(observer)
  }

  private def receive(observer: ActorRef[MemorizingEcho.Signal]): Behavior[Message] = Behaviors.receive {
    (context, message) =>
      message match {
        case Tick =>
          given Timeout = Timeout(2.seconds)
          context.ask[MemorizingEcho.Signal, MemorizingEcho.Reply](
            observer,
            me => MemorizingEcho.Signal(System.currentTimeMillis().toString, me)
          ) {
            case Success(MemorizingEcho.Reply(message)) => Echo(message)
            case Failure(exception)                     => ObservationFailure(exception.getMessage)
          }
        case Echo(text)                  => context.log.info(s"Echoed: $text")
        case ObservationFailure(message) => context.log.error(s"UH-OH! $message")
      }
      Behaviors.same
  }
}

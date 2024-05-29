package erd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

object MemorizingEcho {
  final case class Signal(text: String, replyTo: ActorRef[Reply])
  final case class Reply(text: String)

  def apply(): Behavior[Signal] = Behaviors.receive { (context, message) =>
    context.log.info(s"Got ${message.text}")
    message.replyTo ! Reply(message.text)
    Behaviors.same
  }
}

object Clock {
  sealed trait Message
  private case object Tick                                     extends Message
  private final case class Echo(text: String)                  extends Message
  private final case class ObservationFailure(message: String) extends Message

  def apply(observer: ActorRef[MemorizingEcho.Signal]): Behavior[Message] = setup(observer)

  private def setup(observer: ActorRef[MemorizingEcho.Signal]): Behavior[Message] = Behaviors.withTimers { timers =>
    timers.startTimerWithFixedDelay(Tick, FiniteDuration(8, TimeUnit.SECONDS))
    receive(observer)
  }

  private def receive(observer: ActorRef[MemorizingEcho.Signal]): Behavior[Message] = Behaviors.receive {
    (context, message) =>
      message match {
        case Tick =>
          given Timeout = Timeout(2, TimeUnit.SECONDS)
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

object Root {
  def apply(): Behavior[Unit] = Behaviors.setup { context =>
    val observer = context.spawn(MemorizingEcho(), "observer")
    val clock    = context.spawn(Clock(observer), "clock")
    Behaviors.same
  }
}

@main def run(): Unit = {
  val system = ActorSystem(Root(), "demo", config = ConfigFactory.load("app.conf"))
}

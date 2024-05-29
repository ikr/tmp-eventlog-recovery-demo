package erd

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object MemorizingEcho {
  final case class Signal(text: String, replyTo: ActorRef[Reply])
  final case class Reply(text: String)

  def apply(): Behavior[Signal] = Behaviors.receive { (context, message) =>
    context.log.debug("Got {}", message.text)
    message.replyTo ! Reply(message.text)
    Behaviors.same
  }
}

object Clock {
  case object Tick

  def apply(): Behavior[Tick.type] = setup()

  private def setup(): Behavior[Tick.type] = Behaviors.withTimers { timers =>
    timers.startTimerWithFixedDelay(Tick, FiniteDuration(4, TimeUnit.SECONDS))
    receive()
  }

  private def receive(): Behavior[Tick.type] = Behaviors.receiveMessage { _ =>
    Behaviors.same
  }
}

object Root {
  def apply(): Behavior[Unit] = Behaviors.setup { context =>
    Behaviors.same
  }
}

@main def run(): Unit = {
  val system = ActorSystem(Root(), "demo", config = ConfigFactory.load("app.conf"))
}

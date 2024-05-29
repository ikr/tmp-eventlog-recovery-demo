package erd

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory

object MemorizingEcho {
  final case class Request(text: String, replyTo: ActorRef[Response])
  final case class Response(text: String)

  def apply(): Behavior[Request] = Behaviors.receive { (context, message) =>
    context.log.debug("Got {}", message.text)
    message.replyTo ! Response(message.text)
    Behaviors.same
  }
}

object Root {
  def apply(): Behavior[Unit] = Behaviors.setup { context =>
    Behaviors.stopped
  }
}

@main def run(): Unit = {
  val system = ActorSystem(Root(), "demo", config = ConfigFactory.load("app.conf"))
}

package erd

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object MemorizingEcho {
  final case class Signal(text: String, replyTo: ActorRef[Reply])
  final case class Reply(text: String)

  def apply(): Behavior[Signal] = Behaviors.receive { (context, message) =>
    context.log.info(s"Got ${message.text}")
    message.replyTo ! Reply(message.text)
    Behaviors.same
  }
}

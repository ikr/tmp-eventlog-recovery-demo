package erd

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object MemorizingEcho {
  private sealed trait Event
  private final case class SignalSaved(text: String) extends Event

  sealed trait Command
  final case class Signal(text: String, replyTo: ActorRef[Reply]) extends Command

  final case class Reply(text: String)

  private final case class State(history: Seq[String])

  def apply(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("observer"),
      emptyState = State(Seq()),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )

  private def commandHandler: EventSourcedBehavior.CommandHandler[Command, Event, State] = (state, command) =>
    command match {
      case Signal(text, replyTo) => Effect.persist(SignalSaved(text)).thenReply(replyTo)((_: State) => Reply(text))
    }

  private def eventHandler: EventSourcedBehavior.EventHandler[State, Event] = (state: State, event: Event) =>
    event match {
      case SignalSaved(text) => state.copy(history = state.history :+ text)
    }
}

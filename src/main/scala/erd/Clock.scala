package erd

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef, EntityTypeKey}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Clock {
  sealed trait Message
  private case object Tick                                     extends Message
  private final case class Echo(text: String)                  extends Message
  private final case class ObservationFailure(message: String) extends Message

  def apply(): Behavior[Message] = setup()

  private def setup(): Behavior[Message] = Behaviors.withTimers { timers =>
    timers.startTimerWithFixedDelay(Tick, 10.seconds)
    receive()
  }

  private def receive(): Behavior[Message] = Behaviors.receive { (context, message) =>
    message match {
      case Tick =>
        given Timeout = Timeout(2.seconds)
        val sharding  = ClusterSharding(context.system)
        val f         = recipient(sharding).ask(me => MemorizingEcho.Signal(System.currentTimeMillis().toString, me))
        context.pipeToSelf(f) {
          case Success(MemorizingEcho.Reply(message)) => Echo(message)
          case Success(x)                             => ObservationFailure(s"Unexpected reply $x")
          case Failure(exception)                     => ObservationFailure(exception.getMessage)
        }
      case Echo(text)                  => context.log.info(s"Echoed: $text")
      case ObservationFailure(message) => context.log.error(s"BOOM! $message")
    }
    Behaviors.same
  }

  private def recipient(sharding: ClusterSharding): EntityRef[MemorizingEcho.Command] = {
    val typeKey = EntityTypeKey[MemorizingEcho.Command]("Echo")
    sharding.entityRefFor(typeKey, "echo")
  }
}

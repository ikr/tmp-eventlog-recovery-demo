package erd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.typesafe.config.ConfigFactory

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

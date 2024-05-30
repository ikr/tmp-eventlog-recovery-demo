package erd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}
import akka.persistence.jdbc.testkit.scaladsl.SchemaUtils
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Root {
  def apply(): Behavior[Unit] = Behaviors.setup { context =>
    Await.result(SchemaUtils.createIfNotExists()(context.system.classicSystem), 2.seconds)
    val observer = context.spawn(
      Behaviors.supervise(MemorizingEcho()).onFailure[Exception](SupervisorStrategy.restart),
      "observer"
    )
    context.spawn(Clock(observer), "clock")
    Behaviors.same
  }
}

@main def run(): Unit =
  ActorSystem(Root(), "demo", config = ConfigFactory.load("app.conf"))

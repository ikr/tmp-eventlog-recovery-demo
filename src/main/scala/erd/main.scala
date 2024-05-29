package erd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.persistence.jdbc.testkit.scaladsl.SchemaUtils
import com.typesafe.config.ConfigFactory

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Root {
  def apply(): Behavior[Unit] = Behaviors.setup { context =>
    Await.result(SchemaUtils.createIfNotExists()(context.system.classicSystem), Duration(1, TimeUnit.SECONDS))
    val observer = context.spawn(MemorizingEcho(), "observer")
    context.spawn(Clock(observer), "clock")
    Behaviors.same
  }
}

@main def run(): Unit =
  ActorSystem(Root(), "demo", config = ConfigFactory.load("app.conf"))

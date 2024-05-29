package erd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.persistence.jdbc.testkit.scaladsl.SchemaUtils
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

object Root {
  def apply(): Behavior[Unit] = Behaviors.setup { context =>
    given ExecutionContext = context.system.executionContext

    SchemaUtils.createIfNotExists()(context.system.classicSystem).foreach { _ =>
      val observer = context.spawn(MemorizingEcho(), "observer")
      context.spawn(Clock(observer), "clock")
    }
    Behaviors.same
  }
}

@main def run(): Unit = ActorSystem(Root(), "demo", config = ConfigFactory.load("app.conf"))

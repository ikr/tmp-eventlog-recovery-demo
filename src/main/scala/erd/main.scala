package erd

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.jdbc.testkit.scaladsl.SchemaUtils
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Root {
  def apply(): Behavior[Unit] = Behaviors.setup { context =>
    Await.result(SchemaUtils.createIfNotExists()(context.system.classicSystem), 2.seconds)
    val observer = context.spawn(MemorizingEcho(), "observer")
    context.spawn(Clock(observer), "clock")
    Behaviors.same
  }
}

@main def run(): Unit = {
  val system   = ActorSystem(Root(), "demo", config = ConfigFactory.load("cluster.conf"))
  val sharding = ClusterSharding(system)
}

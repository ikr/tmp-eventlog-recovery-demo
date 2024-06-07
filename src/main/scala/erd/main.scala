package erd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.typesafe.config.ConfigFactory

object Root {
  def apply(): Behavior[Unit] = Behaviors.setup { context =>
    context.spawn(ClusterListener(), "ClusterListener")
    Behaviors.same
  }
}

@main def run(): Unit = {
  startup("Provider", 0)
  startup("Provider", 1)
  startup("Consumer", 2)
}

def startup(role: String, portDelta: Int): Unit = {
  val port = 10_000 + portDelta
  // Override the configuration of the port
  val config = ConfigFactory
    .parseString(s"""
         |akka.cluster.roles = ["$role"]
         |akka.remote.artery.canonical.port = $port
         |""".stripMargin)
    .withFallback(ConfigFactory.load("cluster.conf"))

  // Create an Akka system
  ActorSystem(Root(), "demo", config)
}

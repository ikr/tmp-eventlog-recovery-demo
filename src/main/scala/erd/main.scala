package erd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.sharding.typed.ClusterShardingSettings
import akka.cluster.sharding.typed.ClusterShardingSettings.PassivationStrategySettings
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.persistence.jdbc.testkit.scaladsl.SchemaUtils
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Root {
  def apply(role: String): Behavior[Unit] = Behaviors.setup { context =>
    context.spawn(ClusterListener(), "ClusterListener")

    if (role == "Consumer") {
      Await.result(SchemaUtils.createIfNotExists()(context.system.classicSystem), 8.seconds)
      context.spawn(Clock(), "clock")
    }

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
  val config = ConfigFactory
    .parseString(s"""
         |akka.cluster.roles = ["$role"]
         |akka.remote.artery.canonical.port = $port
         |""".stripMargin)
    .withFallback(ConfigFactory.load("cluster.conf"))

  val system              = ActorSystem(Root(role), "demo", config)
  val sharding            = ClusterSharding(system)
  val defaultSettings     = ClusterShardingSettings(system)
  val passivationSettings = withDisablePassivation(defaultSettings.passivationStrategySettings)

  sharding.init(
    Entity(EchoTypeKey)(createBehavior = entityContext => MemorizingEcho(entityContext.entityId))
      .withSettings(defaultSettings.withPassivationStrategy(passivationSettings))
      .withRole("Provider")
  )
}

private def withDisablePassivation(oldSettings: PassivationStrategySettings): PassivationStrategySettings =
  new PassivationStrategySettings(
    idleEntitySettings = None,
    activeEntityLimit = oldSettings.activeEntityLimit,
    replacementPolicySettings = oldSettings.replacementPolicySettings,
    admissionSettings = oldSettings.admissionSettings
  )

private val EchoTypeKey = EntityTypeKey[MemorizingEcho.Command]("Echo")

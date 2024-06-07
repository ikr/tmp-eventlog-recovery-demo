ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "3.4.2"

val AkkaJdbcVersion       = "5.4.1"
val AkkaManagementVersion = "1.5.2"
val AkkaVersion           = "2.9.3"
val LogbackVersion        = "1.5.6"
val PostgresVersion       = "42.7.3"
val SlickVersion          = "3.5.1"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

libraryDependencies ++= Seq(
  "ch.qos.logback"      % "logback-classic"             % LogbackVersion,
  "com.lightbend.akka" %% "akka-persistence-jdbc"       % AkkaJdbcVersion,
  "com.typesafe.akka"  %% "akka-cluster-sharding-typed" % AkkaVersion,
  "com.typesafe.akka"  %% "akka-persistence-testkit"    % AkkaVersion % Test,
  "com.typesafe.akka"  %% "akka-persistence-typed"      % AkkaVersion,
  "com.typesafe.slick" %% "slick"                       % SlickVersion,
  "com.typesafe.slick" %% "slick-hikaricp"              % SlickVersion,
  "org.postgresql"      % "postgresql"                  % PostgresVersion
)

lazy val root = (project in file("."))
  .settings(
    name := "eventlog-recovery-demo"
  )

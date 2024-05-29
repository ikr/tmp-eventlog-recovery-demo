ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "3.4.2"

val AkkaVersion = "2.9.3"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "eventlog-recovery-demo"
  )

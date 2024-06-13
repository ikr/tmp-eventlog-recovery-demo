ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "3.3.3"

val AkkaJdbcVersion       = "5.2.1"
val AkkaManagementVersion = "1.5.2"
val AkkaVersion           = "2.8.5"
val LogbackVersion        = "1.5.6"
val PostgresVersion       = "42.7.3"
val SlickVersion          = "3.4.1"

libraryDependencies ++= Seq(
  "ch.qos.logback"       % "logback-classic"             % LogbackVersion,
  ("com.lightbend.akka" %% "akka-persistence-jdbc"       % AkkaJdbcVersion).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.akka"  %% "akka-cluster-sharding-typed" % AkkaVersion).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.akka"  %% "akka-persistence-testkit"    % AkkaVersion % Test).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.akka"  %% "akka-persistence-typed"      % AkkaVersion).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.slick" %% "slick"                       % SlickVersion).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.slick" %% "slick-hikaricp"              % SlickVersion).cross(CrossVersion.for3Use2_13),
  "org.postgresql"       % "postgresql"                  % PostgresVersion
)

lazy val root = (project in file("."))
  .settings(
    name := "eventlog-recovery-demo"
  )


import sbt._
import Versions._
trait LibsDependencies{
  val logger = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val config = "com.typesafe" % "config" % "1.3.4"
  val commonsio = "commons-io" % "commons-io" % "2.6"
  val codec = "commons-codec" % "commons-codec" % "1.13"

  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.2.0"
  val akkaTyped = "com.typesafe.akka" %% "akka-actor-typed" % AkkaV
  val akkaStreamTyped = "com.typesafe.akka" %% "akka-stream-typed" % AkkaV

  val akkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpV
  val akkaLog = "com.typesafe.akka" %% "akka-slf4j" % AkkaV
  val circeCore = "io.circe" %% "circe-core" % CirceV
  val circeGeneric = "io.circe" %% "circe-generic" % CirceV
  val circeParser = "io.circe" %% "circe-parser" % CirceV
  val slick = "com.typesafe.slick" %% "slick" % SlickV
  val slickHikaricp = "com.typesafe.slick" %% "slick-hikaricp" % SlickV
  val pgSlickCore = "com.github.tminglei" %% "slick-pg_core" % PgSlickV
  val pgSlick = "com.github.tminglei" %% "slick-pg" % PgSlickV
  val pgSlickJson = "com.github.tminglei" %% "slick-pg_circe-json" % PgSlickV
  val pgSlickJts = "com.github.tminglei" %% "slick-pg_jts" % PgSlickV

  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.0-M1" % Test
  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
  val akkaTest = "com.typesafe.akka" %% "akka-testkit" % AkkaV % Test
  val akkaTypedTest = "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaV % Test
  val streamTest = "com.typesafe.akka" %% "akka-stream-testkit" % AkkaV % Test
  val httpTest = "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpV % Test
  val jedis = "redis.clients" % "jedis" % "3.1.0"
}
object Dependencies extends LibsDependencies {

  val libsDependencies:Seq[ModuleID] = Seq(logger,logback,config,commonsio,codec,scalaXml,akkaTyped,
    akkaStreamTyped,akkaHttp,akkaLog,circeCore,circeGeneric,circeParser,slick,slickHikaricp,
    pgSlickCore, pgSlick,pgSlickJson,pgSlickJts,jedis,scalaTest,scalaCheck,akkaTest,
    akkaTypedTest,streamTest,httpTest)

  val actor = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
  val commonDependencies: Seq[ModuleID] = Seq(actor)
}

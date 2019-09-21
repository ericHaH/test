version in ThisBuild := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.13.1"

organization in ThisBuild:= "com.eric"

scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-encoding",
  "UTF-8",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-language:higherKinds"
)

javacOptions in ThisBuild ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8")

resolvers in ThisBuild += Resolver.bintrayRepo("akka", "snapshots")


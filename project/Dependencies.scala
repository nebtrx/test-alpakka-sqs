import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val akkaStreamsAlpakkaSqs = "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % "0.18"
}

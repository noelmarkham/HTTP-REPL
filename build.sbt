name := "HTTP REPL"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.5",
  "org.http4s" %% "http4s-dsl" % "0.12.0",
  "org.http4s" %% "http4s-blaze-server" % "0.12.0"
)


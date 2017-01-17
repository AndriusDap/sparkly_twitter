name := "tweet clusterer"

version := "1.0"

scalaVersion := "2.11.7"
val twitter4jVersion = "4.0.6"
val sparkVer = "2.0.2"

libraryDependencies ++=  Seq(
  "org.apache.spark" %% "spark-core" % sparkVer,
  "org.apache.spark" %% "spark-sql" % sparkVer,
  "org.apache.spark" %% "spark-core" % sparkVer,
  "org.apache.spark" %% "spark-mllib" % sparkVer,
  "org.apache.spark" %% "spark-sql" % sparkVer,
  "org.apache.spark" %% "spark-streaming" % sparkVer,

  "org.twitter4j" % "twitter4j-core" % twitter4jVersion,
  "org.twitter4j" % "twitter4j-async" % twitter4jVersion,
  "org.twitter4j" % "twitter4j-stream" % twitter4jVersion,
  "com.typesafe" % "config" % "1.3.1",
  "org.eclipse.jetty" % "jetty-server" % "9.4.0.v20161208",
  "org.eclipse.jetty" % "jetty-servlet" % "9.4.0.v20161208",

  "org.eclipse.jetty.websocket" % "websocket-api" % "9.4.0.v20161208",
  "org.eclipse.jetty.websocket" % "websocket-server" % "9.4.0.v20161208",
  "org.eclipse.jetty.websocket" % "websocket-client" % "9.4.0.v20161208"
)
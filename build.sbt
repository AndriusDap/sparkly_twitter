name := "tweet clusterer"

version := "1.0"

scalaVersion := "2.11.7"

val sparkVer = "2.0.2"

libraryDependencies ++=  Seq(
  "org.apache.spark" %% "spark-core" % sparkVer,
  "org.apache.spark" %% "spark-sql" % sparkVer,
  "org.apache.spark" %% "spark-core" % sparkVer,
  "org.apache.spark" %% "spark-mllib" % sparkVer,
  "org.apache.spark" %% "spark-sql" % sparkVer,
  "org.apache.spark" %% "spark-streaming" % sparkVer
)

#!/bin/zsh
sbt package && /opt/spark/bin/spark-submit  --class "App"  target/scala-2.11/tweet-clusterer_2.11-1.0.jar


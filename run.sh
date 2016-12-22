#!/bin/zsh
spark-submit  --class "App" \
  --master local[4] \
  target/scala-2.11/tweet-clusterer_2.11-1.0.jar


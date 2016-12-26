import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object App {
  def main(args: Array[String]) {
    val session = SparkSession.builder().getOrCreate()
    Logger.getRootLogger.setLevel(Level.OFF)

    val tweetSource =  session.read.json("tweets.json").toDF()

    //tweetSource.createOrReplaceTempView("tweetTable")

    val availableTweets = tweetSource.filter(
      tweetSource.col("lang").isNotNull && tweetSource.col("text").isNotNull
    ).filter(
      tweetSource.col("lang") =!= "und"
    ).select(
      tweetSource.col("lang"), tweetSource.col("text")
    )

    val counts = availableTweets.groupBy(
      tweetSource.col("lang")
    ).count

    val sortedCounts = counts.sort(counts.col("count").desc)

    sortedCounts.collect().foreach(println)

    session.stop()
  }
}

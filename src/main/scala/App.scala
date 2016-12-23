import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object App {
  def main(args: Array[String]) {
    val session = SparkSession.builder().getOrCreate()
    Logger.getRootLogger.setLevel(Level.OFF)


    val tweets =  session.read.json("1st_run.json").toDF()

    tweets.createOrReplaceTempView("tweetTable")

    var counts = tweets.filter (
      tweets.col("lang").isNotNull && tweets.col("text").isNotNull
    ).filter(
      tweets.col("lang") =!= "und"
    ).select(
      "lang", "text"
    ).groupBy(
      "lang"
    ).count


    counts.collect().foreach(println)

    session.stop()
  }
}

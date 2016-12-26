import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

case class Tweet(lang: String, text: String)

object App {

  def main(args: Array[String]): Unit = {
    withSpark {
      session =>
        import session.implicits._

        val tweetSource = session.read.json("tweets.json").toDF()

        val availableTweets = tweetSource.filter(
          $"lang".isNotNull
            && $"text".isNotNull
            && $"lang" =!= "und"
        ).select(
          $"lang",
          $"text"
        ).as[Tweet]

        val languages =
          availableTweets
            .groupBy($"lang")
            .count
            .sort($"count".desc)
            .select($"lang")
            .as[String]
            .take(10)

        println(s"Languages we're working with: [${languages.mkString(", ")}]")
        println()

        val tweetsForProcessing = availableTweets.filter($"lang".isin(languages: _*))
        val filteredCount = tweetsForProcessing.count()

        println(s"Selected $filteredCount tweets")
    }
  }

  def withSpark(f: SparkSession => Unit) {
    val session = SparkSession.builder().getOrCreate()
    Logger.getRootLogger.setLevel(Level.OFF)

    f(session)

    session.stop()
  }

}

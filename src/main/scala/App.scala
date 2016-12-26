import java.util.Locale

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.ml.feature.Word2Vec
import org.apache.spark.mllib.clustering.KMeans


case class Tweet(lang: String, text: String)

object App {

  def main(args: Array[String]): Unit = sparkSession {
    session =>
      import session.implicits._
      implicit val s = session

      val availableTweets = session.read.json("tweets.json").toDF()
        .filter($"lang".isNotNull && $"text".isNotNull && $"lang" =!= "und")
        .select($"lang", $"text").as[Tweet]

      val languages = availableTweets
          .groupBy($"lang").count.sort($"count".desc)
          .select($"lang").as[String].take(10)

      println(s"Languages we're working with: [${languages.mkString(", ")}]")
      println()

      val tweetsForProcessing = availableTweets.filter($"lang".isin(languages: _*))

      val singleLang = tweetsForProcessing.filter($"lang" === "en")//languages.head)

      val tweets = cleanUp(tweetsForProcessing)


      val word2Vec = new Word2Vec()
        .setInputCol("value")
        .setOutputCol("result")
        .setVectorSize(8)

      val bags = factorise(tweets.filter(_.lang == "en"))
      bags.printSchema()
      val model = word2Vec.fit(bags)
      val result = model.transform(bags)

      println(s"Selected ${tweetsForProcessing.count()} tweets")
      result.take(10).foreach(println)


      val clusters = KMeans.train(result.select($"result").as[org.apache.spark.mllib.linalg.Vector].rdd, 6, 10)

      

      // Evaluate clustering by computing Within Set Sum of Squared Errors
      val WSSSE = clusters.computeCost(parsedData)
  }

  def cleanUp(dataset: Dataset[Tweet])(implicit session: SparkSession): Dataset[Tweet] = {
    import session.implicits._

    dataset.map {
      case Tweet(lang, text) =>
        Tweet(lang, text.toUpperCase(Locale.forLanguageTag(lang)).replaceAll("[^\\p{L}]", " "))
    }
  }

  def factorise(dataset: Dataset[Tweet])(implicit session: SparkSession): Dataset[Array[String]] = {
    import session.implicits._

    dataset.map(t => t.text.split(" ").filter(_.length > 4))
  }

  def factors(dataset: Dataset[Tweet])(implicit session: SparkSession): Dataset[String] = {
    import session.implicits._

    val words = factorise(dataset).flatMap(c => c)

    println(s"Most popular words are ${
      words
        .groupBy($"value")
        .count
        .sort($"count".desc)
        .select($"value")
        .as[String]
        .take(30)
        .mkString(", ")
    }")

    words.distinct
  }


  def sparkSession(f: SparkSession => Unit) {
    val session = SparkSession.builder().getOrCreate()
    Logger.getRootLogger.setLevel(Level.OFF)
    f(session)
    session.stop()
  }
}

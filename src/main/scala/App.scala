import java.util.Locale

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, Word2Vec}
import org.apache.spark.mllib.clustering.KMeans


case class Tweet(lang: String, text: String)
case class FactorisedTweet(lang: String, tweet: Array[String])

object App {

  def main(args: Array[String]): Unit = sparkSession {
    def report(
                factorisedTweet: Dataset[FactorisedTweet],
                test: Dataset[FactorisedTweet],
                validation: Dataset[FactorisedTweet],
                numTrees: Int = 10
              ): Unit = {
      val word2Vec = new Word2Vec()
        .setInputCol("tweet")
        .setOutputCol("features")
        .setVectorSize(8)

      val indexer = new StringIndexer().setInputCol("lang").setOutputCol("langIndex").fit(factorisedTweet)

      val rf = new RandomForestClassifier()
        .setLabelCol("langIndex")
        .setFeaturesCol("features")
        .setNumTrees(numTrees)

      val labelConverted = new IndexToString().setInputCol("prediction").setOutputCol("predictedLabel").setLabels(indexer.labels)

      val pipeline = new Pipeline().setStages(Array(indexer, word2Vec, rf, labelConverted))

      val model = pipeline.fit(factorisedTweet)
      val predictions = model.transform(test)
      val p_valid = model.transform(validation)

      val evaluator = new MulticlassClassificationEvaluator()
        .setLabelCol("langIndex")
        .setPredictionCol("prediction")
        .setMetricName("accuracy")

      println("Test Error = " + (1.0 - evaluator.evaluate(predictions)))
      println("Validation Error = " + (1.0 - evaluator.evaluate(p_valid)))
    }
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

      val tweets = cleanUp(tweetsForProcessing)

      val factorisedTweet = factorise(tweets)


      val Array(training, test, validation) = factorisedTweet.randomSplit(Array(0.6, 0.2, 0.2))

      report(factorisedTweet, test, validation, 10)
      report(factorisedTweet, test, validation, 200)
  }

  def cleanUp(dataset: Dataset[Tweet])(implicit session: SparkSession): Dataset[Tweet] = {
    import session.implicits._

    dataset.map {
      case Tweet(lang, text) =>
        Tweet(lang, text.toUpperCase(Locale.forLanguageTag(lang)).replaceAll("[^\\p{L}]", " "))
    }
  }

  def factorise(dataset: Dataset[Tweet])(implicit session: SparkSession): Dataset[FactorisedTweet] = {
    import session.implicits._
    dataset.map(t => FactorisedTweet(t.lang, t.text.split(" ").filter(_.length > 4)))
  }

  def sparkSession(f: SparkSession => Unit) {
    val session = SparkSession.builder().getOrCreate()
    Logger.getRootLogger.setLevel(Level.OFF)
    f(session)
    session.stop()
  }
}

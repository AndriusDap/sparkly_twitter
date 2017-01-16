import SparkUtils._
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{RegexTokenizer, StopWordsRemover, Word2Vec}

object Trainer {

  def main(args: Array[String]): Unit = sparkSession {
    session =>
      import session.implicits._
      implicit val s = session

      val tweets = session.read
        .csv("training_tweets.txt")
        .toDF()
        .flatMap {
          row =>
            val string = row.getString(0)
            if(string != null) {
              val line = string.split("\t")
              if (line.length > 2) {
                Some(line(2))
              } else {
                None
              }
            } else {
              None
            }
        }

      val tokenizer = new RegexTokenizer()
        .setInputCol("value")
        .setOutputCol("words")
        .setToLowercase(true)
        .setPattern("\\W")

      val stopWordsRemover = new StopWordsRemover()
        .setInputCol("words")
        .setOutputCol("filtered")
        .setStopWords(StopWordsRemover.loadDefaultStopWords("english"))

      val word2vec = new Word2Vec()
        .setInputCol("filtered")
        .setOutputCol("features")
        .setVectorSize(8)

      val pipeline = new Pipeline().setStages(Array(
        tokenizer,
        stopWordsRemover,
        word2vec
      ))

      val model = pipeline.fit(tweets)

      model.save("model")
      pipeline.save("pipeline")
  }
}

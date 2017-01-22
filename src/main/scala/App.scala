import SparkUtils._
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.linalg.DenseVector
import org.apache.spark.mllib.clustering.StreamingKMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.eclipse.jetty.server.Server

object App {

  def main(args: Array[String]): Unit = sparkSession {
    session =>
      import session.implicits._

      val ssc = new StreamingContext(session.sparkContext, Seconds(3))

      val receiver = new TwitterReceiver("windows")
      val tweets = ssc.receiverStream(receiver)

      val pipeline = PipelineModel.read.load("model")

      val model = new StreamingKMeans()
        .setK(3)
        .setDecayFactor(1.0)
        .setRandomCenters(8, 0.0)


      val factorisedTweets = tweets.transform {
        rdd =>
          pipeline.transform(rdd.toDS()).rdd
      }

      val features = factorisedTweets.transform(
        rdd =>
          rdd.map {
            row =>
              val featuresIndex = row.fieldIndex("features")
              val dense = row.getAs[DenseVector](featuresIndex)
              Vectors.dense(dense.values)
          }
      )

      val texts = factorisedTweets.transform(
        rdd =>
          rdd.map {
            row => row.getAs[String]("value")
          }
      )

      model.trainOn(features)

      val clusters = model.predictOn(features)

      texts.transformWith(clusters, (c: RDD[String], d: RDD[Int]) => {
        d.zip(c)
      }).print()

      ssc.start()
      ssc.awaitTermination()
  }
}

import SparkUtils._
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object App {
  def main(args: Array[String]): Unit = sparkSession {
    session =>
      import session.implicits._

      val ssc = new StreamingContext(session.sparkContext, Seconds(3))

      val receiver = new TwitterReceiver("windows")
      val tweets = ssc.receiverStream(receiver)

      val pipeline = PipelineModel.read.load("model")
      tweets.foreachRDD {
        rdd =>
          val f = pipeline.transform(rdd.toDS())

          f.map(_.mkString).foreach(s => println(s))
      }

      ssc.start()
      ssc.awaitTermination()
  }
}

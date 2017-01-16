
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

object App {
  def main(args: Array[String]): Unit = sparkSession {
    session =>


      val ssc = new StreamingContext(session.sparkContext, Seconds(3))

      val receiver = new TwitterReceiver("windows")
      val tweets = ssc.receiverStream(receiver)

      tweets.print()
      ssc.start()
      ssc.awaitTermination()
  }

  def sparkSession(f: SparkSession => Unit) {
    val session = SparkSession.builder().master("local[*]").getOrCreate()


    Logger.getRootLogger.setLevel(Level.WARN)
    f(session)
    session.stop()
  }
}

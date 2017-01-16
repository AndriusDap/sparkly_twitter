import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object SparkUtils {

  def sparkSession(f: SparkSession => Unit) {
    val session = SparkSession.builder().master("local[*]").getOrCreate()
    Logger.getRootLogger.setLevel(Level.WARN)
    f(session)
    session.stop()
  }
}

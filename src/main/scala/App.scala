import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SimpleApp {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Application")
    val sc = new SparkContext(conf)
    sc.stop()
  }
}


import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import twitter4j.conf.ConfigurationBuilder


case class Tweet(lang: String, text: String)
case class FactorisedTweet(lang: String, tweet: Array[String])

object App {

  val properties = ConfigFactory.load

  println(properties.entrySet())

  val consumerKey = properties.getString("oauth.consumerKey")
  val consumerSecret = properties.getString("oauth.consumerSecret")
  val accessToken = properties.getString("oauth.accessToken")
  val accessTokenSecret = properties.getString("oauth.accessTokenSecret")

  def main(args: Array[String]): Unit = {
    import twitter4j._

    val config = new ConfigurationBuilder()

    config.setDebugEnabled(true)
      .setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
      .setOAuthAccessToken(accessToken)
      .setOAuthAccessTokenSecret(accessTokenSecret)


    val twitterStream = new TwitterStreamFactory(config.build).getInstance()

    twitterStream.addListener(new StatusListener {
      override def onStallWarning(warning: StallWarning): Unit = ()
      override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = ()
      override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = ()
      override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = ()
      override def onException(ex: Exception): Unit = {
        println(ex.getMessage)
      }

      override def onStatus(status: Status): Unit = {
        println(status.getText)
      }
    })

    twitterStream.sample("en")
  }

  def sparkSession(f: SparkSession => Unit) {
    val session = SparkSession.builder().getOrCreate()
    Logger.getRootLogger.setLevel(Level.OFF)
    f(session)
    session.stop()
  }
}

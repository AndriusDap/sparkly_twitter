import com.typesafe.config.ConfigFactory
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import twitter4j._
import twitter4j.conf.ConfigurationBuilder


class TwitterReceiver(filter: String)  extends Receiver[String](StorageLevel.MEMORY_ONLY) {

  val properties = ConfigFactory.load
  val consumerKey = properties.getString("oauth.consumerKey")
  val consumerSecret = properties.getString("oauth.consumerSecret")
  val accessToken = properties.getString("oauth.accessToken")
  val accessTokenSecret = properties.getString("oauth.accessTokenSecret")

  var nullableStream: TwitterStream = _

  override def onStart(): Unit = {
    val config = new ConfigurationBuilder()

    config.setDebugEnabled(true)
      .setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
      .setOAuthAccessToken(accessToken)
      .setOAuthAccessTokenSecret(accessTokenSecret)

    val stream = new TwitterStreamFactory(config.build).getInstance()

    stream.addListener(new StatusListener {
      override def onStatus(status: Status): Unit = {
        if(status.getLang == "en") {
          store(status.getText.replaceAll("\\n", " "))
        }
      }

      override def onStallWarning(warning: StallWarning): Unit = ()
      override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = ()
      override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = ()
      override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = ()
      override def onException(ex: Exception): Unit = ()
    })


    stream.filter(filter)

    nullableStream = stream
  }

  override def onStop(): Unit = {
    println("stopping")
    if(nullableStream != null) {
      nullableStream.shutdown()
      nullableStream = null
    }
  }
}

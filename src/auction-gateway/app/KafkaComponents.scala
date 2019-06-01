import cakesolutions.kafka.KafkaProducer
import cakesolutions.kafka.KafkaProducer.Conf
import com.appodealx.exchange.common.services.kafka.CirceSerializer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import services.KafkaTopicSettings

import scala.concurrent.Future


trait KafkaComponents {

  def configuration: Configuration

  def applicationLifecycle: ApplicationLifecycle

  lazy val kafkaTopicSettings = configuration.get[KafkaTopicSettings]("kafka.topics")

  lazy val kafkaProducer = {
    val bootstrapServers = configuration.get[String]("kafka.bootstrap.servers")

    val configMap = Map[String, AnyRef](
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> bootstrapServers,
      ProducerConfig.ACKS_CONFIG -> "all",
      ProducerConfig.RETRIES_CONFIG -> "0",
      ProducerConfig.BATCH_SIZE_CONFIG -> "16384",
      ProducerConfig.LINGER_MS_CONFIG -> "1",
      ProducerConfig.BUFFER_MEMORY_CONFIG -> "33554432",
      ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG -> "900000" // 15 mins (metadata refresh 10 mins)
    )

    val conf = Conf(configMap, new StringSerializer, new CirceSerializer)

    KafkaProducer(conf)
  }

  applicationLifecycle.addStopHook { () =>
    Future.successful(kafkaProducer.close())
  }
}

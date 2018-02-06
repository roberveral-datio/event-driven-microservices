package com.datio.eda.notification

import java.util.Date

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import com.datio.eda.notification.slack.SlackWebhookClient
import com.datio.eda.{UserCreated, UserNotified}
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.producer.ProducerRecord
import play.api.libs.ws.ahc.AhcWSClient
import play.mvc.Http

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Service which sends notifications to a Slack channel for each
  * registered User.
  */
object Main extends App {

  /*
    * Actor system initialization, required by Akka Streams
    */
  implicit val system: ActorSystem = ActorSystem("notification-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val logger: LoggingAdapter = system.log

  val slackWebhookClient = SlackWebhookClient(AhcWSClient(),
    system.settings.config.getString("slack.webhook"),
    system.settings.config.getString("slack.channel"))

  val usersTopic: String = system.settings.config.getString("kafka.producer.users.topic")

  /*
    * Create the settings for the Kafka consumer and producer. The consumer fetch
    * 'SpecificRecord' which is the superclass for all the Avro generated classes in
    * order to be able to manage different type of messages.
    *
    * The serializers are not set in this object to let the default Kafka properties do it.
    * Otherwise, the KafkaAvroSerializer will not be instantiated properly (missing properties
    * like the schema.registry.url).
    */
  val consumerSettings = ConsumerSettings[Long, SpecificRecord](system, None, None)
  val producerSettings = ProducerSettings[Long, UserNotified](system, None, None)

  // Subscribe to the 'users' topic
  Consumer.committableSource(consumerSettings, Subscriptions.topics(usersTopic))
    // We're only interested in UserCreated events.
    .filter(_.record.value().isInstanceOf[UserCreated])
    .mapAsync(1) { msg =>
      val user = msg.record.value.asInstanceOf[UserCreated]
      logger.info(s"Read!! Type: ${user.getClass.getName} | Value: $user")
      // For each event, we send a welcome message to the Slack webhook
      for {
        status <- slackWebhookClient.sendMessage(s"Welcome ${user.getFirstName} ${user.getLastName}!! " +
          s"You can use ${user.getEmail} for everything you need!")
        if status == Http.Status.OK
      } yield
        // The message carries the offset in order to commit it once stored (at least one)
        ProducerMessage.Message(new ProducerRecord[Long, UserNotified](usersTopic, user.getId,
          UserNotified.newBuilder()
            .setId(user.getId)
            .setTimestamp(new Date().getTime)
            .build()), msg.committableOffset)
    }
    // Store the resulting UserNotified event in the 'users' topic and commit the consumer offset
    .runWith(Producer.commitableSink(producerSettings))
}

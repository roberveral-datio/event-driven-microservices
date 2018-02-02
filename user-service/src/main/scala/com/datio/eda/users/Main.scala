package com.datio.eda.users

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.datio.eda.{UserCreated, UserNotified}
import com.datio.eda.users.repository.UsersRepository
import com.datio.eda.users.routes.UserRoutes
import com.datio.eda.users.table.Users
import com.datio.eda.users.utils.ConfigUtils
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.producer.KafkaProducer
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Service to register users in the system.
  */
object Main extends App
  with UserRoutes
  with ConfigUtils {

  // Intialize actor system for Akka HTTP and Akka Streams
  implicit val system: ActorSystem = ActorSystem("user-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // Create a connector to the internal database (read view, the source of truth is Levana)
  val db: Database = Database.forConfig("dbmem", system.settings.config)
  override val usersRepository: UsersRepository = UsersRepository(db)
  db.run(Users.table.schema.create)

  override val usersTopic: String = system.settings.config.getString("kafka.producer.users.topic")

  // Instantiate a producer for sending UserCreated events
  override val producer =
    new KafkaProducer[Long, UserCreated](propsFromConfig(system.settings.config.getConfig("kafka.producer.config")))

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

  /*
    * Create the settings for the Kafka consumer. The consumer fetch
    * 'SpecificRecord' which is the superclass for all the Avro generated classes in
    * order to be able to manage different type of messages.
    *
    * The serializers are not set in this object to let the default Kafka properties do it.
    * Otherwise, the KafkaAvroSerializer will not be instantiated properly (missing properties
    * like the schema.registry.url).
    */
  val consumerSettings = ConsumerSettings[Long, SpecificRecord](system, None, None)
  // Read events from the 'users' topic
  Consumer.committableSource(consumerSettings, Subscriptions.topics(usersTopic))
    // We're interested in UserNotified events
    .filter(_.record.value().isInstanceOf[UserNotified])
    .mapAsync(1) { msg =>
      val userId = msg.record.value.asInstanceOf[UserNotified].getId
      // Update notification status for the user in the read DB.
      for {
        actualUser <- usersRepository.getById(userId) if actualUser.isDefined
        _ <- usersRepository.updateById(userId, actualUser.get.copy(notified = true))
      } yield msg
    }
    .mapAsync(1) { msg =>
      // Commit the message offset once the DB is updated (at least once)
      msg.committableOffset.commitScaladsl()
    }
    .recover {
      case e => println(e)
    }
    .runWith(Sink.ignore)
}

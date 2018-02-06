package com.datio.eda.users.routes

import java.util.Date

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.datio.eda.UserCreated
import com.datio.eda.users.model.User
import com.datio.eda.users.repository.UsersRepository
import com.datio.eda.users.utils.ProducerUtils
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json.{JsError, JsSuccess, Json}

/**
  * Contains the routes for registering and querying users.
  */
trait UserRoutes extends ProducerUtils {
  val usersTopic: String
  val producer: KafkaProducer[Long, UserCreated]
  val usersRepository: UsersRepository
  implicit val logger: LoggingAdapter

  val route: Route = pathPrefix("users") {
    pathPrefix(LongNumber) { id =>
      get {
        // Query user by ID
        onSuccess(usersRepository.getById(id)) {
          case Some(user) =>
            complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, Json.prettyPrint(Json.toJson(user))))
          case None =>
            complete(StatusCodes.NotFound)
        }
      }
    } ~
    get {
      // Query all users
      onSuccess(usersRepository.getAll) { users =>
        complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, Json.prettyPrint(Json.toJson(users))))
      }
    } ~
    post {
      // Registers a new user (parses the input as JSON an validates as User to see if it's malformed)
      entity(as[String]) { rawUser =>
        Json.parse(rawUser).validate[User] match {
          case JsSuccess(user, _) =>
            onSuccess(usersRepository.getById(user.id)) {
              case Some(_) => complete(StatusCodes.Conflict)
              case None =>
                // We send an UserCreated event to the 'users' topic
                producer.send(new ProducerRecord[Long, UserCreated](usersTopic, user.id,
                  UserCreated.newBuilder()
                    .setId(user.id)
                    .setFirstName(user.firstName)
                    .setLastName(user.lastName)
                    .setAge(user.age)
                    .setEmail(user.email)
                    .setTimestamp(new Date().getTime)
                    .build()), printSendResult)
                complete(StatusCodes.Created, HttpEntity(ContentTypes.`application/json`,
                  Json.prettyPrint(Json.toJson(user.copy(notified = false)))))
            }
          case JsError(errors) =>
            complete(StatusCodes.BadRequest)
        }
      }
    }
  }

}

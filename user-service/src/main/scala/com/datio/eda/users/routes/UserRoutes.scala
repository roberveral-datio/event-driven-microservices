package com.datio.eda.users.routes

import java.util.Date

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.datio.eda.UserCreated
import com.datio.eda.users.model.User
import com.datio.eda.users.repository.UsersRepository
import com.datio.eda.users.utils.ProducerUtils
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json.{JsError, JsSuccess, Json}

trait UserRoutes extends ProducerUtils {
  val usersTopic: String
  val producer: KafkaProducer[Long, UserCreated]
  val usersRepository: UsersRepository

  val route: Route = pathPrefix("users") {
    pathPrefix(LongNumber) { id =>
      get {
        onSuccess(usersRepository.getById(id)) {
          case Some(user) =>
            complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, Json.prettyPrint(Json.toJson(user))))
          case None =>
            complete(StatusCodes.NotFound)
        }
      }
    } ~
    get {
      onSuccess(usersRepository.getAll) { users =>
        complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, Json.prettyPrint(Json.toJson(users))))
      }
    } ~
    post {
      entity(as[String]) { rawUser =>
        Json.parse(rawUser).validate[User] match {
          case JsSuccess(user, _) =>
            onSuccess(usersRepository.save(user.copy(notified = false))) { id =>
              // Once stored in the internal DB, send an UserCreated event to the 'users' topic
              producer.send(new ProducerRecord[Long, UserCreated](usersTopic, id,
                UserCreated.newBuilder()
                  .setId(id)
                  .setFirstName(user.firstName)
                  .setLastName(user.lastName)
                  .setAge(user.age)
                  .setEmail(user.email)
                  .setTimestamp(new Date().getTime)
                  .build()), printSendResult)

              complete(StatusCodes.Created, HttpEntity(ContentTypes.`application/json`,
                Json.prettyPrint(Json.toJson(user.copy(id = id, notified = false)))))
            }
          case JsError(errors) =>
            complete(StatusCodes.BadRequest)
        }
      }
    }
  }

}

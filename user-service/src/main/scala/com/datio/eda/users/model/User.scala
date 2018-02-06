package com.datio.eda.users.model

import play.api.libs.json.{Format, Json}

/**
  * Models a User in the service.
  *
  * @param id        id of the User.
  * @param firstName first name of the User.
  * @param lastName  last name of the User.
  * @param age       user's age.
  * @param email     user's email.
  * @param notified  flag set to true if the notification for the user
  *                  registration have been sent.
  */
case class User(id: Long,
                firstName: String,
                lastName: String,
                age: Int,
                email: String,
                notified: Boolean = false)

object User {

  /**
    * Conversion between [[User]] and JSON.
    */
  implicit val userFormat: Format[User] = Json.using[Json.WithDefaultValues].format[User]
}

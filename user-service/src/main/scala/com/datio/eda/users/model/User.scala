package com.datio.eda.users.model

import play.api.libs.json.{Format, Json}

case class User(id: Long = 0,
                firstName: String,
                lastName: String,
                age: Int,
                email: String,
                notified: Boolean = false) extends BaseEntity

object User {
  implicit val userFormat: Format[User] = Json.using[Json.WithDefaultValues].format[User]
}

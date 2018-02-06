package com.datio.eda.users.table

import com.datio.eda.users.model.User
import slick.jdbc.H2Profile.api._

/**
  * Table used for the 'users' materialized view.
  *
  * Although Kafka is the source of truth, we generate a materialized
  * view of users in an in-memory database which is updated according
  * to the user related events.
  *
  * @param tag slick table tag.
  */
class Users(tag: Tag) extends Table[User](tag, "users") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey)
  def firstName = column[String]("first_name")
  def lastName = column[String]("last_name")
  def age = column[Int]("age")
  def email = column[String]("email")
  def notified = column[Boolean]("notified", O.Default(false))

  def * = (id, firstName, lastName, age, email, notified) <> ((User.apply _).tupled, User.unapply)
}

object Users {
  val table = TableQuery[Users]
}

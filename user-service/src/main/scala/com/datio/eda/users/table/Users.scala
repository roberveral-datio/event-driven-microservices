package com.datio.eda.users.table

import com.datio.eda.users.model.User
import slick.jdbc.H2Profile.api._

class Users(tag: Tag) extends BaseTable[User](tag, "users") {
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

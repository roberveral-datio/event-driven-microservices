package com.datio.eda.users.repository

import com.datio.eda.users.model.User
import com.datio.eda.users.table.Users
import slick.jdbc.H2Profile.api._

case class UsersRepository(db: Database) extends BaseRepository[Users, User] {
  override val query = Users.table
}

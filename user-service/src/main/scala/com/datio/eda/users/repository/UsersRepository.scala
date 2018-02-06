package com.datio.eda.users.repository

import com.datio.eda.users.model.User
import com.datio.eda.users.table.Users
import slick.jdbc.H2Profile.api._

import scala.concurrent.Future

/**
  * Repository for storing and retrieving users from the
  * materialized view (local DB).
  *
  * @param db database to use as user repository.
  */
case class UsersRepository(db: Database) {
  val query = Users.table

  /**
    * Obtains all the registered users.
    *
    * @return sequence of stored users.
    */
  def getAll: Future[Seq[User]] = {
    db.run(query.result)
  }

  /**
    * Obtains a concrete User given its id.
    *
    * @param id id of the user.
    * @return the user registered with the given id or None if there
    *         isn't any user with that id.
    */
  def getById(id: Long): Future[Option[User]] = {
    db.run(query.filter(_.id === id).result.headOption)
  }

  /**
    * Stores a user in the repository.
    *
    * @param row user to store.
    * @return the id of the stored user.
    */
  def save(row: User): Future[Int] = {
    db.run(query += row)
  }

  /**
    * Updates a user in the repository.
    *
    * @param id id of the user to update.
    * @param row new user to store instead of the previous one.
    * @return number of rows affected in the update.
    */
  def updateById(id: Long, row: User): Future[Int] = {
    db.run(query.filter(_.id === id).update(row))
  }

  /**
    * Deletes a user from the repository.
    *
    * @param id id of the user to remove.
    * @return number of rows affected in the delete.
    */
  def deleteById(id: Long): Future[Int] = {
    db.run(query.filter(_.id === id).delete)
  }
}

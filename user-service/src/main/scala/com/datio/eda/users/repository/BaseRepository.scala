package com.datio.eda.users.repository

import com.datio.eda.users.model.BaseEntity
import com.datio.eda.users.table.BaseTable
import slick.dbio.Effect
import slick.jdbc.H2Profile.api._
import slick.lifted.CanBeQueryCondition
import slick.sql.FixedSqlAction

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait BaseRepositoryQuery[T <: BaseTable[E], E <: BaseEntity] {
  val query: TableQuery[T]


  def getByIdQuery(id: Long): Query[T, E, Seq] = {
    query.filter(_.id === id)
  }

  def getAllQuery = {
    query
  }

  def filterQuery[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Query[T, E, Seq] = {
    query.filter(expr)
  }

  def saveQuery(row: E): FixedSqlAction[Long, NoStream, Effect.Write] = {
    query returning query.map(_.id) += row
  }

  def deleteByIdQuery(id: Long): FixedSqlAction[Int, NoStream, Effect.Write] = {
    query.filter(_.id === id).delete
  }

  def updateByIdQuery(id: Long, row: E): FixedSqlAction[Int, NoStream, Effect.Write] = {
    query.filter(_.id === id).update(row)
  }

}

trait BaseRepository[T <: BaseTable[E], E <: BaseEntity]
  extends BaseRepositoryQuery[T, E] {

  val db: Database

  def getAll: Future[Seq[E]] = {
    db.run(getAllQuery.result)
  }

  def getById(id: Long): Future[Option[E]] = {
    db.run(getByIdQuery(id).result.headOption)
  }

  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Seq[E]] = {
    db.run(filterQuery(expr).result)
  }

  def save(row: E): Future[Long] = {
    db.run(saveQuery(row))
  }

  def updateById(id: Long, row: E): Future[Int] = {
    db.run(updateByIdQuery(id, row))
  }

  def deleteById(id: Long): Future[Int] = {
    db.run(deleteByIdQuery(id))
  }
}

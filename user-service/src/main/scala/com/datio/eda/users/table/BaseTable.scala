package com.datio.eda.users.table

import slick.jdbc.H2Profile.api._

abstract class BaseTable[E](tag: Tag, tableName: String)
  extends Table[E](tag, tableName) {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
}

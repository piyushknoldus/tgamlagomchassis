package com.knoldus.helpers

import com.typesafe.config.{ Config, ConfigFactory }
import Constants._

object ConfigHelper {

  private lazy val config: Config = ConfigFactory.load()

  val numberOfShards: Long = config.getLong(NUMBER_OF_SHARDS)

}

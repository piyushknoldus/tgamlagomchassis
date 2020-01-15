package com.knoldus.api.helpers

import com.typesafe.config.{Config, ConfigFactory}
import com.knoldus.api.helpers.Constants._

object ConfigHelper {

  private lazy val config: Config = ConfigFactory.load()

  val numberOfShards: Long = config.getLong(NUMBER_OF_SHARDS)

}

package com.knoldus.api.impl

import com.knoldus.external.UserMessage
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable


object SerializationRegistry extends JsonSerializerRegistry{


  override def serializers: immutable.Seq[JsonSerializer[_]] = {
    immutable.Seq(
      JsonSerializer.compressed(UserMessage.kafkaMessageFormat)
    )
  }

}

package com.knoldus.external


import play.api.libs.json.{Format, Json}

case class UserMessage(
                        id: Long,
                        name: String,
                        date: String,
                        message: String
                      )

object UserMessage {

  implicit val kafkaMessageFormat: Format[UserMessage] = Json.format

}

case class KafkaMessageWithMetadata(
                                     userMessage: UserMessage,
                                     inboundKafkaTimestamp: Option[Long]
                                   )

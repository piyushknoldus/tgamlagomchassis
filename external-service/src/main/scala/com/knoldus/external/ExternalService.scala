package com.knoldus.external

import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service}
import com.lightbend.lagom.scaladsl.api.Service.{ named, topic }

trait ExternalService extends Service {

  def inboundTopic: Topic[UserMessage]

  final override def descriptor: Descriptor = {
    named("external-service")
    .withTopics(
    topic("external-messages-1", this.inboundTopic)
    ).withAutoAcl(true)
  }

}

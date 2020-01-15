package com.knoldus.api.app

import akka.actor.ActorRef
import com.knoldus.api.api.ProcessorService
import com.knoldus.api.impl.{ProcessorActorShard, ProcessorServiceImpl, SerializationRegistry}
import com.knoldus.api.subscriber.KafkaSubscriber
import com.knoldus.external.ExternalService
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import play.api.libs.ws.ahc.AhcWSComponents
import scala.concurrent.ExecutionContext


trait ProcessorApplicationComponents extends LagomServerComponents with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  override lazy val lagomServer: LagomServer = serverFor[ProcessorService](wire[ProcessorServiceImpl])

  lazy val jsonSerializerRegistry: JsonSerializerRegistry = SerializationRegistry

}

abstract class LagomProcessorApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with ProcessorApplicationComponents
    with AhcWSComponents
    with LagomKafkaComponents {

  lazy val nimbleExternalService: ExternalService = serviceClient.implement[ExternalService]

  lazy val processorActorRef: ActorRef = ProcessorActorShard.processorRegion(actorSystem)

  wire[KafkaSubscriber]
}

class ProcessorApplication extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
    new LagomProcessorApplication(context) with LagomDevModeComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new LagomProcessorApplication(context) with LagomDevModeComponents

  override def describeService: Option[Descriptor] = Some(
    readDescriptor[ProcessorService]
  )
}

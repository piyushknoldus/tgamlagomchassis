package com.knoldus.subscriber

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import akka.{ Done, NotUsed }
import com.knoldus.impl.ProcessorActor.ProcessUserMessage
import com.knoldus.external.{ ExternalService, KafkaMessageWithMetadata, UserMessage }
import com.knoldus.kamon.KamonFactory
import com.lightbend.lagom.scaladsl.api.broker.Message
import com.lightbend.lagom.scaladsl.broker.kafka.KafkaMetadataKeys

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Try

class KafkaSubscriber(
    externalService: ExternalService,
    actorRef: ActorRef
) extends FlowHelper {

  override val processorActorRef: ActorRef = actorRef

  // Can be dynamic by fetching value from configuration
  val consumerGroup = "consumer-group"

  implicit val timeout = akka.util.Timeout(5, TimeUnit.SECONDS)

  /**
   * Start consuming messages from the kafka topic
   * Where inbound topic is the topic from where we need to consume messages
   * subscribe is used to obtain a subscriber to this topic
   * withGroupId returns A copy of this subscriber with the passed group id
   * withMetadata returns this subscriber, but message payloads are wrapped in [[Message]] instances to allow
   * --- accessing any metadata associated with the message.
   * atLeastOnce : Applies the passed `flow` to the messages processed by this subscriber. Messages are delivered to the passed
   * * `flow` at least once.
   */
  externalService.inboundTopic.subscribe.withGroupId(consumerGroup).withMetadata.atLeastOnce {
    kafkaMessageFlow
  }

}

trait FlowHelper extends KamonFactory {

  implicit val timeOut: Timeout = Timeout(5000.milli)
  val processorActorRef: ActorRef
  val parallelism = 8

  val terminateFlow: Flow[Any, Done, NotUsed] = Flow[Any].map(_ => Done)

  val forwardKafkaMessageToWorker: Flow[KafkaMessageWithMetadata, Done, NotUsed] = Flow[KafkaMessageWithMetadata]
    .mapAsync(parallelism) { kafkaMessageWithMeta =>

      (processorActorRef ? ProcessUserMessage(kafkaMessageWithMeta.userMessage.id.toString, kafkaMessageWithMeta.userMessage))
        .map(_ => Done)
        .recover {
          case ex: Exception =>
            print("\nException found while waiting for processor response: " + ex)
            Done
        }
    }

  val processKafkaMessageFlow: Flow[KafkaMessageWithMetadata, Done, NotUsed] = Flow[KafkaMessageWithMetadata]
    .map(kafkaMessageWithMetadata => {
      counter.increment()
      print("\n\nafter counter\n\n\n\n")
      print(s"\nProcessing kafka message: $kafkaMessageWithMetadata")
      kafkaMessageWithMetadata
    })
    .via(forwardKafkaMessageToWorker)
    .via(terminateFlow)

  val kafkaMessageFlow: Flow[Message[UserMessage], Done, NotUsed] = Flow[Message[UserMessage]]
    .map { msg =>
      val messageKey = Try(msg.messageKeyAsString).toOption
      val kafkaHeaders = msg.get(KafkaMetadataKeys.Headers)
      val offset = msg.get(KafkaMetadataKeys.Offset).getOrElse(0L)
      val partition = msg.get(KafkaMetadataKeys.Partition).getOrElse(0)
      val kafkaTimestamp = msg.get(KafkaMetadataKeys.Timestamp)

      val kafkaMessageWithMetadata: KafkaMessageWithMetadata = KafkaMessageWithMetadata(msg.payload, inboundKafkaTimestamp = kafkaTimestamp)
      print(s"\nInbound Kafka message arrived: Message: [$kafkaMessageWithMetadata] Key: [$messageKey] Headers: [$kafkaHeaders]," +
        s" partition: [$partition], offset: [$offset], inboundKafkaTimestamp: $kafkaTimestamp at: ${LocalDateTime.now}")

      kafkaMessageWithMetadata
    }
    .via(processKafkaMessageFlow)
    .via(terminateFlow)
}

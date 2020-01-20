package com.knoldus.impl

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }
import com.knoldus.impl.ProcessorActor.{ Command, ProcessUserMessage }
import com.knoldus.external.UserMessage
import com.knoldus.helpers.ConfigHelper

class ProcessorActor extends Actor {

  override def receive: Receive = {

    case msg: Command => msg match {
      case pum: ProcessUserMessage =>
        print("\nMessage found by actor : " + pum)

      case msg => print(s"\nGot unknown message : $msg")
    }

  }

}

object ProcessorActor {

  val numberOfShards: Long = ConfigHelper.numberOfShards
  val extractEntityId: ShardRegion.ExtractEntityId = {
    case command: Command => command.id -> command
  }
  val extractShardId: ShardRegion.ExtractShardId = {
    case command: Command => shard(command.id)
  }

  def apply(system: ActorSystem): ActorRef = {
    system.actorOf(Props[ProcessorActor])
  }

  private def shard(id: String): String = {
    math.abs(id.hashCode % numberOfShards).toString
  }

  trait Command {
    val id: String
  }

  case class ProcessUserMessage(id: String, message: UserMessage) extends Command

}

object ProcessorActorShard {

  def processorRegion(system: ActorSystem): ActorRef = ClusterSharding(system).start(
    typeName = "processor",
    entityProps = Props[ProcessorActor],
    settings = ClusterShardingSettings(system),
    extractEntityId = ProcessorActor.extractEntityId,
    extractShardId = ProcessorActor.extractShardId
  )

}


package io.netflow
package actors

import java.net.InetSocketAddress
import java.time.LocalDateTime

import io.netflow.actors.FlowManager.FlowManagerActor
import io.netflow.lib._

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}

/**
 * This spawns just as many workers as configured
 */
private[netflow] object FlowManager {
  object FlowManagerActor {
    def props(netFlowReceiver: NetFlowReceiver) = Props(new FlowManagerActor(netFlowReceiver))
  }

  class FlowManagerActor(netFlowReceiver: NetFlowReceiver) extends Actor with ActorLogging {
    log info "Starting up"

    private def workerName(i: Int) = f"flowWorker$i%02d"

    private val flowWorkers = (1 to NodeConfig.values(context.system).cores).map(i =>
      context.actorOf(FlowWorker.props(netFlowReceiver), workerName(i)))

    private def sendToNextFlowWorker(flowCounter: Long)(msg: Any): Unit = {
      val act = flowWorkers((flowCounter % flowWorkers.size).toInt)
      act ! msg
      context become normal(flowCounter + 1)
    }

    override def receive: Receive = normal(0)

    private def normal(flowCounter: Long): Receive = {
      case bd: BadDatagram => sendToNextFlowWorker(flowCounter)(bd)
      case sj: SaveJob => sendToNextFlowWorker(flowCounter)(sj)
    }
  }
}

private[netflow] class FlowManager(val actor: ActorRef) {
  def this(arf: ActorRefFactory, netFlowReceiver: NetFlowReceiver)
          (props: Props = FlowManagerActor.props(netFlowReceiver), name: String = "flowManager") =
    this(arf.actorOf(props, name))

  def bad(sender: InetSocketAddress): Unit = actor ! BadDatagram(LocalDateTime.now, sender.getAddress)
  def save(sender: InetSocketAddress, flowPacket: FlowPacket): Unit = actor ! SaveJob(sender, flowPacket)
}

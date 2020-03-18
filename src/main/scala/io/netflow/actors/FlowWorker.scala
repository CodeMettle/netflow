package io.netflow.actors

import io.netflow.NetFlowReceiver
import io.netflow.lib.{BadDatagram, SaveJob}

import akka.actor.{Actor, ActorLogging, Props}

/**
  * Created by steven on 4/2/2018.
  */
object FlowWorker {
  def props(netFlowReceiver: NetFlowReceiver) = Props(new FlowWorker(netFlowReceiver))
}

class FlowWorker(netFlowReceiver: NetFlowReceiver) extends Actor with ActorLogging {
  override def receive: Receive = {
    case BadDatagram(date, from) => log.warning("Bad data from {} at {}", from, date)

    case SaveJob(fromAddr, packet) => netFlowReceiver.receivedPacket(fromAddr, packet)
  }
}

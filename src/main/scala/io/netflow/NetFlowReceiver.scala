package io.netflow

import java.net.InetSocketAddress

import io.netflow.lib.FlowPacket

/**
  * Created by steven on 4/2/2018.
  */
trait NetFlowReceiver {
  def receivedPacket(fromAddr: InetSocketAddress, flowPacket: FlowPacket): Unit
}

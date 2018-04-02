package io.netflow.lib

import java.net.InetSocketAddress

case class SaveJob(
  sender: InetSocketAddress,
  flowPacket: FlowPacket/*,
  prefixes: List[InetPrefix]*/)

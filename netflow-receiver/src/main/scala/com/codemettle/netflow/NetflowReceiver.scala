package com.codemettle.netflow

import java.net.InetSocketAddress

import io.netflow.lib.FlowPacket
import io.netflow.{netflowParser, NetFlowV9TemplateDAO}

import com.codemettle.streamutil.{IngestingResult, UdpStreamUtil}

import akka.actor.ActorSystem
import akka.stream.KillSwitches
import akka.stream.alpakka.udp.Datagram
import akka.stream.alpakka.udp.scaladsl.Udp
import akka.stream.scaladsl.{Flow, Keep}
import scala.concurrent.Future

/**
  * @author steven
  *
  */
object NetflowReceiver {
  def apply(bindAddress: InetSocketAddress, handler: Flow[FlowPacket, _, _], v9TemplateDAO: NetFlowV9TemplateDAO)(
      implicit system: ActorSystem
  ): Future[IngestingResult] = {
    import system.dispatcher

    val unwrap = Flow[Datagram].map(d => d.remote -> d.data)
    val parse = netflowParser(unwrap, v9TemplateDAO)

    val flow = parse
      .via(handler)
      .map(_ => Option.empty[Datagram])
      .collect {
        case Some(d) => d
      }
      .viaMat(KillSwitches.single)(Keep.right)

    UdpStreamUtil.futureAndKillSwitchToResult(Udp.bindFlow(bindAddress).joinMat(flow)(Keep.both).run())
  }
}

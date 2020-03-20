package io

import java.net.InetSocketAddress

import io.netflow.flows.cflow
import io.netflow.lib.FlowPacket
import io.netty.buffer.Unpooled

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.alpakka.udp.Datagram
import akka.stream.alpakka.udp.scaladsl.Udp
import akka.stream.scaladsl.{Flow, Keep}
import akka.stream.{KillSwitch, KillSwitches}
import akka.util.ByteString
import scala.concurrent.Future
import scala.util.Try

/**
  * Created by steven on 3/30/2018.
  */
package object netflow {

  def Tryo[T](t: => T): Option[T] = Try(t).toOption

  def netflowReceiver(
      bindAddress: InetSocketAddress,
      handler: Flow[FlowPacket, _, _],
      v9TemplateDAO: NetFlowV9TemplateDAO
  )(implicit system: ActorSystem): (Future[InetSocketAddress], KillSwitch) = {
    val unwrap = Flow[Datagram].map(d => d.remote -> d.data)
    val parse = netflowParser(unwrap, v9TemplateDAO)

    val flow = parse
      .via(handler)
      .map(_ => Option.empty[Datagram])
      .collect {
        case Some(d) => d
      }
      .viaMat(KillSwitches.single)(Keep.right)

    Udp.bindFlow(bindAddress).joinMat(flow)(Keep.both).run()
  }

  def netflowParser[In, Mat](
      incomingPackets: Flow[In, (InetSocketAddress, ByteString), Mat],
      v9TemplateDAO: NetFlowV9TemplateDAO
  )(implicit system: ActorSystem): Flow[In, FlowPacket, Mat] = {
    import system.dispatcher

    val log = Logging(system, "NetflowParser")
    val templateHolder = new HackyTemplateHolder(v9TemplateDAO)

    incomingPackets
      .mapAsync(Runtime.getRuntime.availableProcessors()) {
        case (addr, bytes) => templateHolder.handlerFor(addr.getAddress).map(handler => (addr, bytes, handler))
      }
      .map {
        case (osender, bytes, handler) =>
          val buf = Unpooled.wrappedBuffer(bytes.toArray)

          val ret = Tryo(buf.getUnsignedShort(0)) match {
            case Some(1)  => cflow.NetFlowV1Packet(osender, buf).toOption
            case Some(5)  => cflow.NetFlowV5Packet(osender, buf).toOption
            case Some(6)  => cflow.NetFlowV6Packet(osender, buf).toOption
            case Some(7)  => cflow.NetFlowV7Packet(osender, buf).toOption
            case Some(9)  => cflow.NetFlowV9Packet(osender, buf, handler).toOption
            case Some(10) => log.info("We do not handle NetFlow IPFIX yet"); None
            case _        => None
          }

          buf.release()

          ret
      }
      .collect {
        case Some(p) => p
      }
  }
}

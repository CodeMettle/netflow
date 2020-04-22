package com.codemettle.netflow

import java.net.InetAddress

import io.netflow.NetFlowV9TemplateDAO
import io.netflow.flows.cflow.NetFlowV9Template
import io.netflow.lib.FlowPacket

import com.codemettle.streamutil.AbstractIngestingTest

import akka.stream.scaladsl.Flow
import scala.concurrent.Future

/**
  * @author steven
  *
  */
class TestNetflowReceiver extends AbstractIngestingTest {
  private val noopFlow = Flow[FlowPacket]

  private val noopDAO: NetFlowV9TemplateDAO = new NetFlowV9TemplateDAO {
    override def loadTemplates(forAddr: InetAddress): Future[Iterable[NetFlowV9Template]] = Future.successful(Nil)
    override def persistTemplates(forAddr: InetAddress, templates: Iterable[NetFlowV9Template]): Future[Unit] =
      Future.unit
  }

  "NetflowReceiver" should "handle bind errors" in {
    ingestTest(a => NetflowReceiver(a, noopFlow, noopDAO))
  }
}

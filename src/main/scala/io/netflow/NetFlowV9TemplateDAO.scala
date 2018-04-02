package io.netflow

import java.net.InetAddress

import io.netflow.flows.cflow

import scala.concurrent.Future

/**
  * Created by steven on 4/2/2018.
  */
trait NetFlowV9TemplateDAO {
  def loadTemplates(forAddr: InetAddress): Future[Iterable[cflow.NetFlowV9Template]]
  def persistTemplates(forAddr: InetAddress, templates: Iterable[cflow.NetFlowV9Template]): Future[Unit]
}

package io.netflow

import java.net.InetSocketAddress

import akka.stream.KillSwitch

/**
  * @author steven
  *
  */
sealed trait IngestingResult
case class Ingesting(boundTo: InetSocketAddress, ks: KillSwitch) extends IngestingResult
case object BindFailure extends IngestingResult
case class OtherFailure(t: Throwable) extends IngestingResult

package com.codemettle.streamutil

import java.net.InetSocketAddress

import akka.stream.KillSwitch
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author steven
  *
  */
object UdpStreamUtil {
  def futureAndKillSwitchToResult(
      in: (Future[InetSocketAddress], KillSwitch)
  )(implicit ec: ExecutionContext): Future[IngestingResult] =
    StreamUtil.futureAndKillSwitchToResult(
      in,
      t => t.isInstanceOf[IllegalArgumentException] && Option(t.getMessage).exists(_.startsWith("Unable to bind to"))
    )
}

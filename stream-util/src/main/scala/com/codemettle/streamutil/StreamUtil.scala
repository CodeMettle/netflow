package com.codemettle.streamutil

import java.net.{BindException, InetSocketAddress}

import akka.stream.{BindFailedException, KillSwitch}
import akka.stream.scaladsl.Tcp
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author steven
  *
  */
object StreamUtil {
  def futureAndKillSwitchToResult(in: (Future[InetSocketAddress], KillSwitch), isBindFailure: (Throwable) => Boolean)(
      implicit ec: ExecutionContext
  ): Future[IngestingResult] = {
    val (f, ks) = in
    // if we have to recover there's probably no reason to shutdown the killswitch, but shouldn't hurt
    f.map(Ingesting(_, ks)).recover {
      case t if isBindFailure(t) =>
        ks.shutdown()
        BindFailure

      case t =>
        ks.shutdown()
        OtherFailure(t)
    }
  }

  def tcpFutureAndKillSwitchToResult(
      in: (Future[Tcp.ServerBinding], KillSwitch)
  )(implicit ec: ExecutionContext): Future[IngestingResult] = {
    val (sbF, ks) = in
    futureAndKillSwitchToResult(
      sbF.map(_.localAddress) -> ks,
      t =>
        t.isInstanceOf[BindFailedException] || ((t.getCause ne null) && t.getCause
          .isInstanceOf[BindException] || t.getCause.isInstanceOf[BindFailedException])
    )
  }
}

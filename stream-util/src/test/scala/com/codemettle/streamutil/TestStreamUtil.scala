package com.codemettle.streamutil

import akka.stream.KillSwitches
import akka.stream.scaladsl.{Flow, Keep, Sink, Tcp}
import akka.util.ByteString

/**
  * @author steven
  *
  */
class TestStreamUtil extends AbstractIngestingTest {
  import system.dispatcher

  private val noopFlow = Flow[ByteString]

  "StreamUtil" should "handle bind errors in TCP" in {
    ingestTest { a =>
      val connections = Tcp().bind(a.getHostString, a.getPort).viaMat(KillSwitches.single)(Keep.both)
      StreamUtil.tcpFutureAndKillSwitchToResult(
        connections
          .to(Sink.foreach { connection =>
            connection.flow.join(noopFlow).run()
          })
          .run()
      )
    }
  }
}

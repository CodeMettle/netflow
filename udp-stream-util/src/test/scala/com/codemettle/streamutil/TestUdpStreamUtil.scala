package com.codemettle.streamutil

import akka.stream.KillSwitches
import akka.stream.alpakka.udp.Datagram
import akka.stream.alpakka.udp.scaladsl.Udp
import akka.stream.scaladsl.{Flow, Keep}

/**
  * @author steven
  *
  */
class TestUdpStreamUtil extends AbstractIngestingTest {
  import system.dispatcher

  "UdpStreamUtil" should "handle bind errors in UDP" in {
    ingestTest { a =>
      UdpStreamUtil.futureAndKillSwitchToResult(
        Udp.bindFlow(a).joinMat(Flow[Datagram].viaMat(KillSwitches.single)(Keep.right))(Keep.both).run()
      )
    }
  }
}

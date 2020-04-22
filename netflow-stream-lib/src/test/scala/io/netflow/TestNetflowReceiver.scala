package io.netflow

import java.net.{InetAddress, InetSocketAddress}

import com.typesafe.config.ConfigFactory
import io.netflow.flows.cflow.NetFlowV9Template
import io.netflow.lib.FlowPacket
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import akka.testkit.TestKit
import scala.concurrent.Future

/**
  * @author steven
  *
  */
class TestNetflowReceiver(_system: ActorSystem)
    extends TestKit(_system)
    with AsyncFlatSpecLike
    with Matchers
    with BeforeAndAfterAll {
  import system.dispatcher

  def this() = this(ActorSystem("test", ConfigFactory.parseString("akka.loglevel=OFF")))

  override protected def afterAll(): Unit =
    shutdown(verifySystemShutdown = true)

  private val bindAddr = new InetSocketAddress("localhost", 0)
  private val noopFlow = Flow[FlowPacket]

  private val noopDAO: NetFlowV9TemplateDAO = new NetFlowV9TemplateDAO {
    override def loadTemplates(forAddr: InetAddress): Future[Iterable[NetFlowV9Template]] = Future.successful(Nil)
    override def persistTemplates(forAddr: InetAddress, templates: Iterable[NetFlowV9Template]): Future[Unit] =
      Future.unit
  }

  private def runTest(creator: (InetSocketAddress, Flow[FlowPacket, _, _]) => Future[IngestingResult]) =
    creator(bindAddr, noopFlow).flatMap { ir =>
      ir shouldBe an[Ingesting]
      val Ingesting(bound, ks) = ir.asInstanceOf[Ingesting]

      creator(bound, noopFlow).map { ir =>
        ks.shutdown()

        ir shouldEqual BindFailure
      }
    }

  "NetflowReceiver" should "handle bind errors in UDP" in {
    runTest((a, f) => netflowReceiver(a, f, noopDAO))
  }
}

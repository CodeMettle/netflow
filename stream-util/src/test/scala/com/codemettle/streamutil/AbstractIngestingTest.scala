package com.codemettle.streamutil

import java.net.InetSocketAddress

import com.typesafe.config.ConfigFactory
import org.scalatest.{Assertion, BeforeAndAfterAll}
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

import akka.actor.ActorSystem
import akka.testkit.TestKit
import scala.concurrent.Future

/**
  * @author steven
  *
  */
abstract class AbstractIngestingTest(_system: ActorSystem)
    extends TestKit(_system)
    with AsyncFlatSpecLike
    with Matchers
    with BeforeAndAfterAll {
  import system.dispatcher

  def this() = this(ActorSystem("test", ConfigFactory.parseString("akka.loglevel=OFF")))

  override protected def afterAll(): Unit =
    shutdown(verifySystemShutdown = true)

  protected val bindAddr = new InetSocketAddress("localhost", 0)

  protected def ingestTest(creator: (InetSocketAddress) => Future[IngestingResult]): Future[Assertion] =
    creator(bindAddr).flatMap { ir =>
      ir shouldBe an[Ingesting]
      val Ingesting(bound, ks) = ir.asInstanceOf[Ingesting]

      creator(bound).map { ir =>
        ks.shutdown()

        ir shouldEqual BindFailure
      }
    }
}

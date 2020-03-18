package io.netflow
package actors

import java.net.InetSocketAddress

import io.netflow.actors.SenderWorker.{Init, TemplatesFetched, TemplatesSaved}
import io.netflow.flows._
import io.netflow.lib._
import io.netflow.storage.FlowSender

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Status}
import akka.pattern.pipe
import scala.concurrent.duration._

private[netflow] object SenderWorker {
  def props(config: FlowSender, templatesDAO: NetFlowV9TemplateDAO, flowManager: FlowManager) =
    Props(new SenderWorker(config, templatesDAO, flowManager))

  private case object Init
  private case class TemplatesFetched(tpmls: Iterable[cflow.NetFlowV9Template])
  private case object TemplatesSaved
}

private[netflow] class SenderWorker(config: FlowSender, templatesDAO: NetFlowV9TemplateDAO, flowManager: FlowManager)
  extends Actor with cflow.NetFlowV9Packet.TemplateHandler with ActorLogging {

  import context.dispatcher

//  private[actors] val senderPrefixes = new AtomicReference(config.prefixes)

  override def preStart(): Unit = {
    super.preStart()

    self ! Init
  }

  private implicit def system: ActorSystem = context.system

  private var templateCache = Map.empty[Int, cflow.NetFlowV9Template]

//  def templates = templateCache
//  def setTemplate(tmpl: cflow.Template): Unit = templateCache += tmpl.number -> tmpl
//  private var cancellable = Shutdown.schedule()

  private def handleFlowPacket(osender: InetSocketAddress, handled: Option[FlowPacket]) = {
    /*if (NodeConfig.values.storage.isDefined)*/ handled match {
      case Some(fp) =>
        flowManager.save(osender, fp/*, senderPrefixes.get.toList*/)
      case _ =>
//        warn("Unable to parse FlowPacket")
        flowManager.bad(osender)
    }
  }

  override def templateFor(flowsetId: Int): Option[cflow.NetFlowV9Template] = templateCache get flowsetId

  override def storeTemplate(t: cflow.NetFlowV9Template): Unit = {
    templateCache += (t.number -> t)
    templatesDAO.persistTemplates(config.ip, templateCache.values).map(_ => TemplatesSaved) pipeTo self
  }

  override def receive: Receive = init(Nil)

  private def init(requestingWhenReady: List[SenderManager.GetActor]): Receive = {
    case ga: SenderManager.GetActor => context become init(ga :: requestingWhenReady)

    case Init => templatesDAO loadTemplates config.ip map TemplatesFetched pipeTo self

    case Status.Failure(t) =>
      log.error(t, "Error fetching existing templates for {}, retrying", config.ip)
      context.system.scheduler.scheduleOnce(1.second, self, Init)

    case TemplatesFetched(templs) =>
      templateCache = templs.map(x => x.number -> x).toMap
      log.info("Starting up with templates: {}", templateCache.keys.mkString(", "))
      requestingWhenReady.map(_.p).foreach(_.success(self))
      context become normal
  }

  private def normal: Receive = {
    case SenderManager.GetActor(p, _) => p.success(self)

    case TemplatesSaved => log.debug("saved templates")

    case Status.Failure(t) => log.error(t, "Error saving templates")

    case NetFlow(osender, buf) =>
//      Shutdown.avoid()
      val handled: Option[FlowPacket] = {
        Tryo(buf.getUnsignedShort(0)) match {
          case Some(1) => cflow.NetFlowV1Packet(osender, buf).toOption
          case Some(5) => cflow.NetFlowV5Packet(osender, buf).toOption
          case Some(6) => cflow.NetFlowV6Packet(osender, buf).toOption
          case Some(7) => cflow.NetFlowV7Packet(osender, buf).toOption
          case Some(9) => cflow.NetFlowV9Packet(osender, buf, this).toOption
          case Some(10) =>
            log.info("We do not handle NetFlow IPFIX yet"); None //Some(cflow.NetFlowV10Packet(sender, buf))
          case _ => None
        }
      }
      buf.release()
//      if (NodeConfig.values.netflow.persist) handled.foreach(_.persist())
      handleFlowPacket(osender, handled)

/*
    case Shutdown =>
      info("Shutting down")
      SenderManager.removeActorFor(config.ip)
      templateCache = Map.empty
      this ! Wactor.Die
*/
  }

/*
  private case object Shutdown {
    def schedule() = scheduleOnce(Shutdown, 5.minutes)
    def avoid() {
      cancellable.cancel()
      cancellable = schedule()
    }
  }
*/
}

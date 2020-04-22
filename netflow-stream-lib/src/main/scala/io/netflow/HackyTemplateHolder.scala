package io.netflow

import java.net.InetAddress

import io.netflow.HackyTemplateHolder.HackyTemplateHandler
import io.netflow.flows.cflow.NetFlowV9Packet.TemplateHandler
import io.netflow.flows.cflow.NetFlowV9Template

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.after
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * @author steven
  *
  */
object HackyTemplateHolder {
  private class HackyTemplateHandler(addr: InetAddress,
                                     initial: Iterable[NetFlowV9Template],
                                     v9TemplateDAO: NetFlowV9TemplateDAO,
                                     log: LoggingAdapter)(implicit system: ActorSystem)
      extends TemplateHandler {
    import system.dispatcher

    private var templates = initial.map(t => t.number -> t).toMap

    override def templateFor(flowsetId: Int): Option[NetFlowV9Template] = this.synchronized(templates.get(flowsetId))
    override def storeTemplate(t: NetFlowV9Template): Unit = this.synchronized {
      templates += (t.number -> t)
      v9TemplateDAO.persistTemplates(addr, templates.values).onComplete {
        case Success(_) => log.debug("saved templates")
        case Failure(t) => log.error(t, "Error saving templates")
      }
    }
  }
}

class HackyTemplateHolder(v9TemplateDAO: NetFlowV9TemplateDAO)(implicit system: ActorSystem) {
  import system.dispatcher
  private val log = Logging(system, "TemplateHolder")

  private var handlers = Map.empty[InetAddress, Promise[TemplateHandler]]

  def handlerFor(addr: InetAddress): Future[TemplateHandler] = this.synchronized {
    handlers.get(addr).map(_.future).getOrElse {
      val p = Promise[TemplateHandler]()
      handlers += (addr -> p)

      def fetchLoop(): Future[HackyTemplateHandler] =
        v9TemplateDAO
          .loadTemplates(addr)
          .map { templs =>
            log.info("Starting up with templates: {}", templs.map(_.number).mkString(", "))
            new HackyTemplateHandler(addr, templs, v9TemplateDAO, log)
          }
          .recoverWith {
            case t =>
              log.error(t, "Error fetching existing templates for {}, retrying", addr)
              after(1.second, system.scheduler)(fetchLoop())
          }

      p.completeWith(fetchLoop())

      p.future
    }
  }
}

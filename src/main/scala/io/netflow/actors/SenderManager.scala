package io.netflow
package actors

import java.net.InetAddress

import io.netflow.actors.SenderManager.{GetActor, SenderManagerActor}
import io.netflow.storage.FlowSender

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props}
import akka.pattern.gracefulStop
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

private[netflow] object SenderManager {
  private[actors] case class GetActor(p: Promise[ActorRef], addr: InetAddress)

  object SenderManagerActor {
    def props(templatesDAO: NetFlowV9TemplateDAO, flowManager: FlowManager) =
      Props(new SenderManagerActor(templatesDAO, flowManager))
  }

  class SenderManagerActor(templatesDAO: NetFlowV9TemplateDAO, flowManager: FlowManager)
    extends Actor with ActorLogging {
    log info "Starting up"

    override def receive: Receive = normal(Map.empty)

    private def normal(senderActors: Map[InetAddress, ActorRef]): Receive = {
      case ga@GetActor(_, forAddr) =>
        senderActors get forAddr match {
          case Some(act) => act ! ga

          case None =>
            val conf = FlowSender(forAddr)
            val act = context.actorOf(
              SenderWorker.props(conf, templatesDAO, flowManager), validActorName(s"senderWorker-$forAddr"))
            act ! ga
            context become normal(senderActors + (forAddr -> act))
        }
    }
  }
}

private[netflow] class SenderManager(templatesDAO: NetFlowV9TemplateDAO, flowManager: FlowManager, name: String)
                                    (implicit arf: ActorRefFactory) {
  private val actor = arf.actorOf(SenderManagerActor.props(templatesDAO, flowManager), name)

  def findActorFor(sender: InetAddress): Future[ActorRef] = {
    val p = Promise[ActorRef]()
    actor ! GetActor(p, sender)
    p.future
  }

  def shutdown(): Future[Boolean] = gracefulStop(actor, 10.seconds)
}

package io.netflow
package lib

import akka.actor.ActorSystem

private[netflow] object NodeConfig {

  case class ServerConfig(netflow: NetFlowConfig)

  case class NetFlowConfig(calculateSamples: Boolean, extraFields: Boolean)

  private var configs = Map.empty[ActorSystem, ServerConfig]
  private def config(implicit system: ActorSystem): ServerConfig =
    configs.getOrElse(system, {
      val conf = load
      configs += (system -> conf)
      conf
    })

  implicit private class RichActorSystem(val system: ActorSystem) extends AnyVal {
    private def conf = system.settings.config

    def getBool(name: String): Option[Boolean] = Tryo(conf.getBoolean(name))
    def getBool(name: String, fallback: Boolean): Boolean = getBool(name) getOrElse fallback
  }

  private def Config(implicit system: ActorSystem) = new RichActorSystem(system)

  private def load(implicit system: ActorSystem): ServerConfig = {

    val netflow = NetFlowConfig(
      calculateSamples = Config.getBool("netflow.calculateSamples", fallback = false),
      extraFields = Config.getBool("netflow.extraFields", fallback = true)
    )

    val server = ServerConfig(netflow = netflow)

    server
  }

  def values(implicit system: ActorSystem): ServerConfig = config

}

package io

import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

/**
  * Created by steven on 3/30/2018.
  */
package object netflow {
  private val invalidActorNameRegex = """[^a-zA-Z0-9_.*+:@&=,!~';-]""".r

  def validActorName(name: String): String = invalidActorNameRegex.replaceAllIn(name, "-")

  def Tryo[T](t: ⇒ T): Option[T] = Try(t).toOption
  def Tryo[T](t: ⇒ T, fallback: ⇒ T): T = Try(t).getOrElse(fallback)

  trait Logger extends LazyLogging {
    def debug(s: String): Unit = logger.debug(s)
    def info(s: String): Unit = logger.info(s)
    def warn(s: String): Unit = logger.warn(s)
    def error(s: String): Unit = logger.error(s)
  }
}

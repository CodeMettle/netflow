package io.wasted.util

import java.net.InetSocketAddress

import io.netflow.Tryo

/**
  * Helper Object for creating InetPrefix-objects.
  */
object InetPrefix {
  /**
    * Converts a String to an InetSocketAddress with respects to IPv6 ([123:123::123]:80).
    * @param string IP Address to convert
    * @return InetSocketAddress
    */
  def stringToInetAddr(string: String): Option[InetSocketAddress] = string match {
    case ipv4: String if ipv4.matches("""\d+\.\d+\.\d+\.\d+:\d+""") =>
      val split = ipv4.split(":")
      Tryo(new java.net.InetSocketAddress(split(0), split(1).toInt))
    case ipv6: String if ipv6.matches("""\[[0-9a-fA-F:]+\]:\d+""") =>
      val split = ipv6.split("]:")
      val addr = split(0).replaceFirst("\\[", "")
      Tryo(new java.net.InetSocketAddress(java.net.InetAddress.getByName(addr), split(1).toInt))
    case _ => None
  }

}

package io.netflow.lib

import java.net.InetAddress
import java.time.LocalDateTime

case class BadDatagram(date: LocalDateTime, sender: InetAddress)

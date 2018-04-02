package io.netflow.util

import java.net.{InetAddress, NetworkInterface, SocketException, UnknownHostException}
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong
import java.util.{Set ⇒ _, _}

import scala.annotation.tailrec
import scala.collection.JavaConverters._

/**
  * Created by steven on 3/30/2018.
  */
object UUIDs {
  // http://www.ietf.org/rfc/rfc4122.txt
  private final val StartEpoch = {
    // UUID v1 timestamp must be in 100-nanoseconds interval since 00:00:00.000 15 Oct 1582.
    val c = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"))
    c.set(Calendar.YEAR, 1582)
    c.set(Calendar.MONTH, Calendar.OCTOBER)
    c.set(Calendar.DAY_OF_MONTH, 15)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    c.getTimeInMillis
  }

  private final val MinClockSeqAndNode = 0x8080808080808080L

  private final val ClockSeqAndNode = {
    val clock = new Random(System.currentTimeMillis).nextLong
    val node = {
      val digest = MessageDigest.getInstance("MD5")

      def update(digest: MessageDigest, value: String): Unit =
        Option(value).map(_.getBytes(UTF_8)).foreach(digest.update)

      val allLocalAddresses = {
        var allIps = Set.empty[String]

        try {
          val localhost = InetAddress.getLocalHost
          allIps += localhost.toString
          // Also return the hostname if available, it won't hurt (this does a dns lookup, it's only done once at startup)
          allIps += localhost.getCanonicalHostName

          Option(InetAddress.getAllByName(localhost.getCanonicalHostName)) foreach { allMyIps ⇒
            allIps ++= allMyIps.map(_.toString)
          }
        } catch {
          case _: UnknownHostException ⇒ // Ignore, we'll try the network interfaces anyway
        }

        try {
          Option(NetworkInterface.getNetworkInterfaces).map(_.asScala) foreach { nics ⇒
            allIps ++= nics.flatMap(_.getInetAddresses.asScala.map(_.toString))
          }
        } catch {
          case _: SocketException ⇒ // Ignore, if we've really got nothing so far, we'll throw an exception
        }

        allIps
      }

      for (address ← allLocalAddresses) {
        update(digest, address)
      }

      val props = System.getProperties
      update(digest, props.getProperty("java.vendor"))
      update(digest, props.getProperty("java.vendor.url"))
      update(digest, props.getProperty("java.version"))
      update(digest, props.getProperty("os.arch"))
      update(digest, props.getProperty("os.name"))
      update(digest, props.getProperty("os.version"))

      val hash = digest.digest

      var node = 0L
      (0 until 6) foreach { i ⇒
        node |= ((0x00000000000000ffL & hash(i).toLong) << (i * 8))
      }

      // Since we don't use the mac address, the spec says that multicast
      // bit (least significant bit of the first byte of the node ID) must be 1.
      node | 0x0000010000000000L
    }

    var lsb = 0L
    lsb |= (clock & 0x0000000000003FFFL) << 48
    lsb |= 0x8000000000000000L
    lsb |= node
    lsb
  }

  private def makeMSB(timestamp: Long) = {
    var msb = 0L
    msb |= (0x00000000ffffffffL & timestamp) << 32
    msb |= (0x0000ffff00000000L & timestamp) >>> 16
    msb |= (0x0fff000000000000L & timestamp) >>> 48
    msb |= 0x0000000000001000L // sets the version to 1.
    msb
  }

  private def fromUnixTimestamp(tstamp: Long) = (tstamp - StartEpoch) * 10000

  private val lastTimestamp = new AtomicLong(0L)

  private def millisOf(timestamp: Long) = timestamp / 10000

  private def getCurrentTimestamp: Long = {
    @tailrec
    def loop(): Long = {
      val now = fromUnixTimestamp(System.currentTimeMillis())
      val last = lastTimestamp.get()
      if (now > last) {
        if (lastTimestamp.compareAndSet(last, now))
          now
        else
          loop()
      } else {
        val lastMillis = millisOf(last)
        // If the clock went back in time, bail out
        if (millisOf(now) < lastMillis)
          lastTimestamp.incrementAndGet()
        else {
          val candidate = last + 1
          // If we've generated more than 10k uuid in that millisecond,
          // we restart the whole process until we get to the next millis.
          // Otherwise, we try use our candidate ... unless we've been
          // beaten by another thread in which case we try again.
          if (millisOf(candidate) == lastMillis && lastTimestamp.compareAndSet(last, candidate))
            candidate
          else
            loop()
        }
      }
    }

    loop()
  }


  def startOf(timestamp: Long) = new UUID(makeMSB(fromUnixTimestamp(timestamp)), MinClockSeqAndNode)

  def timeBased() = new UUID(makeMSB(getCurrentTimestamp), ClockSeqAndNode)
}

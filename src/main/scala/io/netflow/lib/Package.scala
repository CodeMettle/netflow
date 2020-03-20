package io.netflow

import java.net.InetAddress

import io.netty.buffer._

import scala.util.{Failure, Success, Try}

package object lib {

  import io.netflow.flows.cflow.TemplateFields

  final val defaultAddr = InetAddress.getByName("0.0.0.0")

  implicit class RichByteBuf(val buf: ByteBuf) extends AnyVal {
    def getInetAddress(template: flows.cflow.Template, field: TemplateFields.Value): Option[InetAddress] = {
      if (!template.hasField(field)) return None
      Some(getInetAddress(template.typeOffset(field), template.typeLen(field)))
    }

    def getInetAddress(template: flows.cflow.Template,
                       field1: TemplateFields.Value,
                       field2: TemplateFields.Value): InetAddress =
      buf.getInetAddress(template, field1) orElse
        buf.getInetAddress(template, field2) getOrElse
        defaultAddr

    def getInetAddress(offset: Int, length: Int = 4): InetAddress =
      Try[InetAddress] {
        val buffer = buf.slice(offset, length)
        val data = (1 to length).map(_ => buffer.readUnsignedByte.toByte).toArray
        InetAddress.getByAddress(data)
      } match {
        case Success(addr) => addr
        case Failure(_)    => defaultAddr
      }

    def getUnsignedInteger(template: flows.cflow.Template, field: TemplateFields.Value): Option[Long] = {
      if (!template.hasField(field)) return None
      Some(getUnsignedInteger(template.typeOffset(field), template.typeLen(field)))
    }

    def getUnsignedInteger(template: flows.cflow.Template,
                           field1: TemplateFields.Value,
                           field2: TemplateFields.Value): Option[Long] =
      getUnsignedInteger(template, field1) orElse getUnsignedInteger(template, field2)

    def getUnsignedInteger(offset: Int, length: Int): Long = length match {
      case 1 => buf.getUnsignedByte(offset).toLong
      case 2 => buf.getUnsignedShort(offset).toLong
      case 3 => buf.getUnsignedMedium(offset).toLong
      case 4 => buf.getUnsignedInt(offset)
      case 8 => buf.getLong(offset) & 0X00000000FFFFFFFFL
      case _ => 0L
    }
  }
}

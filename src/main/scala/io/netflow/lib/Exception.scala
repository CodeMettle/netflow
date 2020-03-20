package io.netflow.lib

// Since these expected errors are quite easy to distinguish and locate, we don't need expensive stacktraces
class FlowException(msg: String) extends Exception(msg) with util.control.NoStackTrace

class InvalidFlowVersionException(version: Int) extends FlowException("Version " + version)

class IncompleteFlowPacketHeaderException extends FlowException("")

class CorruptFlowPacketException extends FlowException("")

class IllegalTemplateIdException(template: Int) extends FlowException("TemplateId " + template)

class IllegalFlowSetLengthException extends FlowException("FlowSetLength (0)")

class ShortFlowPacketException extends FlowException("")

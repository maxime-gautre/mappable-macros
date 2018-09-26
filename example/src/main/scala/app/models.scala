package app

case class CompletedEvent(a: String, b: Int)

case class Message[A](name: String, data: A)

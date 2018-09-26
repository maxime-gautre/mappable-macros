package app

import mappable.Mapify

class MappableListener {

  implicit val completedEventMappable = Mapify.materializeMappable[CompletedEvent]

  def listen(message: Message[CompletedEvent]): Map[String, Any] = {
    MappableLogger.log(message)
  }
}

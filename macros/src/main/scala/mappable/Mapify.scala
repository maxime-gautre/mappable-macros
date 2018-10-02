package mappable

import scala.language.experimental.macros

import MappableMacroImpl.materializeMappableImpl

object Mapify {
  def mappable[T]: Mappable[T] = macro materializeMappableImpl[T]

  def toMap[T: Mappable](t: T): Map[String, Any] = implicitly[Mappable[T]].toMap(t)
}

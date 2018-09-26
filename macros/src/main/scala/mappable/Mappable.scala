package mappable

trait Mappable[T] {
  def toMap(t: T): Map[String, Any]
}

package mappable

import scala.reflect.macros.whitebox

/*
 Mappable macro
 This macro converts a case class (not nested) into a Map[String, Any]
 http://blog.echo.sh/2013/11/04/exploring-scala-macros-map-to-case-class-conversion.html
*/
object MappableMacroImpl {

  def materializeMappableImpl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[Mappable[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val optTpeCtor = typeOf[Option[_]].typeConstructor

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.headOption.toSeq.flatten

    val addEntries = fields.map { field =>
      val name = field.asTerm.name
      val key = name.decodedName.toString

      if (field.info.typeConstructor <:< optTpeCtor) {
        // Option
        q"t.$name.foreach { v => builder += $key -> v }"
      } else {
        q"builder += $key -> t.$name"
      }
    }

    val mappable = q"""
      new _root_.mappable.Mappable[$tpe] {
        def toMap(t: $tpe): Map[String, Any] = {
          val builder = scala.collection.immutable.Map.newBuilder[String, Any]

          ..$addEntries

          builder.result()
        }
      }"""

    c.Expr[Mappable[T]](mappable)
  }
}

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

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.head

    val toMapParams = fields.map { field =>
      val name = field.asTerm.name
      val key = name.decodedName.toString
      q"$key -> t.$name"
    }

    val mappable =
      q"""
        new Mappable[$tpe] {
          def toMap(t: $tpe): Map[String, Any] = Map(..$toMapParams)
        }
      """
    c.Expr[Mappable[T]](mappable)
  }
}

package mappable

import scala.annotation.tailrec
import scala.reflect.macros.whitebox

/*
 Mappable macro
 This macro converts a case class (not nested) into a Map[String, Any]
 http://blog.echo.sh/2013/11/04/exploring-scala-macros-map-to-case-class-conversion.html
*/
object MappableMacroImpl {

  private def getDeclsFields(c: whitebox.Context)(tpe: c.universe.Type): Seq[c.universe.Symbol] = {
    val fields = tpe.decls.collectFirst {
      case m: c.universe.MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.headOption.toSeq.flatten

    fields
  }

  @tailrec
  private def recursive(c: whitebox.Context)(fields: Seq[c.Symbol], currentName: Option[String], res: Seq[c.universe.Tree]): Seq[c.universe.Tree] = {
    import c.universe._

    val optTpeCtor = typeOf[Option[_]].typeConstructor

    fields.headOption match {
      case Some(field) =>
        val name = field.asTerm.name
        val key = name.decodedName.toString
        if (
          field.typeSignature.typeSymbol.isType &&
            field.typeSignature.typeSymbol.asType.isClass &&
            field.typeSignature.typeSymbol.asType.asClass.isCaseClass) {
          recursive(c)(getDeclsFields(c)(field.typeSignature) ++ fields.tail, Some(key), res)
        } else {
          val keyName = currentName.fold(key) { n => s"$n.$key" }
          val newField = if (field.info.typeConstructor <:< optTpeCtor) {
            // Option
            q"t.$name.foreach { v => builder += $keyName -> v }"
          } else {
            q"builder += $keyName -> t.$name"
          }
          recursive(c)(fields.tail, None, res :+ newField)
        }
      case None => res
    }
  }

  def materializeMappableImpl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[Mappable[T]] = {
    import c.universe._
    val tpe: c.Type = weakTypeOf[T]
    val fields = getDeclsFields(c)(tpe)
    val addEntries = recursive(c)(fields, None, Seq.empty[c.universe.Tree])

    val mappable =
      q"""
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

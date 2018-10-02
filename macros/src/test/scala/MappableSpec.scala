package test

import _root_.mappable.{ Mappable, Mapify }

case class Foo(bar: String, lorem: Int)

case class Bar(lorem: Int, o: Option[Double])

final class MappableSpec extends org.specs2.mutable.Specification {
  "Mappable" title

  "Macro" should {
    "materialize conversion for Foo" in {
      implicit val conv: Mappable[Foo] = Mapify.mappable[Foo]

      Mapify.toMap(Foo("lorem", 2)) must_=== Map[String, Any](
        "bar" -> "lorem",
        "lorem" -> 2
      )
    }

    "materialize conversion for Bar" >> {
      implicit val conv: Mappable[Bar] = Mapify.mappable[Bar]

      "with Some" in {
        Mapify.toMap(Bar(1, Some(2.0D))) must_=== Map[String, Any](
          "lorem" -> 1,
          "o" -> 2.0D
        )
      }

      "with None" in {
        Mapify.toMap(Bar(3, None)) must_=== Map[String, Any]("lorem" -> 3)
      }
    }
  }
}

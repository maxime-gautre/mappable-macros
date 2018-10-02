name := "mappableMacros"

version := "0.1"

val commonSettings = Seq(
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq("-deprecation", "-feature"),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )
)

lazy val macros = project.in(file("macros")).
  settings(commonSettings ++ Seq(
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "4.3.2" % Test)
  ))

lazy val example = project.in(file("example")).
  dependsOn(macros).settings(commonSettings)


import sbt._

object Dependencies {

  object autoImport{
    val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
  }

  lazy val monitoringDependencies = Seq(
//    "io.kamon" %% "kamon-bundle" % "2.0.0",
//    "io.kamon" %% "kamon-prometheus" % "2.0.0",
//    "io.kamon" %% "kamon-zipkin" % "1.0.0",
//    "io.kamon" %% "kamon-apm-reporter" % "2.0.0",
    "io.kamon" %% "kamon-prometheus" % "1.1.1",
   "io.kamon" %% "kamon-core" % "1.1.2",

  )
  val logging = "ch.qos.logback" % "logback-classic" % "1.0.1"

}

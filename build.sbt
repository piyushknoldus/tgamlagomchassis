
name := "lagom-kafka-template"

version := "0.1"

scalaVersion := "2.12.6"

import sbt.Keys._
import Dependencies.autoImport._

lazy val `processor` = (project in file("."))
  .aggregate(`processor-api`, `processor-impl`, `external-Service`)

lazy val `processor-api` = (project in file("processor-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

// An external service as kafka producer service
lazy val `external-Service` = (project in file("external-service"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false
lagomCassandraPort in ThisBuild := 9042
lagomKafkaPort in ThisBuild := 9092

lazy val `processor-impl` = (project in file("processor-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      Dependencies.logging
    ) ++ Dependencies.monitoringDependencies
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`processor-api`)
  .dependsOn(`external-Service`)

package com.knoldus.kamon

import kamon.Kamon
import kamon.metric.Counter
import kamon.prometheus.PrometheusReporter

trait KamonFactory {

  val counter: Counter = Kamon.counter("kafka.messages")
  val timer = Kamon.timer("app.timer")
}

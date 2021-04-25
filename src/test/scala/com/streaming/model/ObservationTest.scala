package com.streaming.model

import org.scalatest.flatspec._
import org.scalatest.matchers._

/**
 * @author Spyros Koukas
 */
class ObservationTest extends AnyFlatSpec with should.Matchers {

  "An Observation" should "be trivial to construct" in {
    val label = "Test"
    val observation = new Observation(label)
    observation.actualLabel should be(label)
    observation.observation should be(Map.empty)
  }

  "An Observation" should "provide reliable access to data" in {
    val label = "Test"
    val frequency = 1L
    val observation = new Observation(label, Map(label -> frequency, (label + "2") -> 2L))
    observation.actualLabel should be(label)
    observation.observation.get(label).get should be(frequency)
    observation.observation.size should be(2)
  }
}

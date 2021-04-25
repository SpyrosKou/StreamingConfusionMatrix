package com.streaming.model

import org.scalatest.flatspec._
import org.scalatest.matchers._

/**
 * Basic test, this was mostly created to practice with tests
 *
 * @author Spyros Koukas
 */
class ConfusionMatrixTest extends AnyFlatSpec with should.Matchers {

  "A Confusion Matrix default constructor" should "have no elements in map" in {
    val confusionMatrix = new ConfusionMatrix()
    confusionMatrix.predictions.size should be(0)
  }

  "A Confusion Matrix " should "provide reliable access to data" in {
    val observations = Map("A" -> Map("A" -> 10L,
      "B" -> 2L,
      "C" -> 3L))

    val confusionMatrix = new ConfusionMatrix(observations)
    confusionMatrix.predictions.size should be(1)
    confusionMatrix.predictions.get("A").get.size should be(3)
    confusionMatrix.predictions.get("A").get("A") should be(10L)
    confusionMatrix.predictions.get("A").get("B") should be(2L)
    confusionMatrix.predictions.get("A").get("C") should be(3L)

  }
}

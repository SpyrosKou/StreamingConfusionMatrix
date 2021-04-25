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

  "A Confusion Matrix " should "be updated correctly with Observations" in {
    val observationA = new Observation("A", Map("A" -> 124L, "B" -> 12L))
    val observationB = new Observation("B", Map("A" -> 11L, "B" -> 321L))

    val confusionMatrix = new ConfusionMatrix()
    confusionMatrix.predictions.size should be(0)

    val confusionMatrixA = confusionMatrix.addPrediction(observationA);
    confusionMatrixA.predictions.size should be(1)
    confusionMatrixA.predictions.get("A").get.size should be(2)
    confusionMatrixA.predictions.get("A").get("A") should be(124L)
    confusionMatrixA.predictions.get("A").get("B") should be(12L)

    val confusionMatrixB = confusionMatrixA.addPrediction(observationB);
    confusionMatrixB.predictions.size should be(2)
    confusionMatrixB.predictions.get("B").get.size should be(2)

    confusionMatrixB.predictions.get("A").get("A") should be(124L)
    confusionMatrixB.predictions.get("A").get("B") should be(12L)
    confusionMatrixB.predictions.get("B").get("A") should be(11L)
    confusionMatrixB.predictions.get("B").get("B") should be(321L)

    val confusionMatrixC = confusionMatrixB.removePrediction(observationB);
    confusionMatrixC.predictions.size should be(1)
    confusionMatrixC.predictions.get("A").get.size should be(2)
    confusionMatrixC.predictions.get("A").get("A") should be(124L)
    confusionMatrixC.predictions.get("A").get("B") should be(12L)


    val confusionMatrixD = confusionMatrixC.removePrediction(observationB);
    confusionMatrixD.predictions.size should be(0)
  }
}

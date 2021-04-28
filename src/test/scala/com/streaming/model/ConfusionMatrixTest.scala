/**
 * StreamingConfusionMatrixCalculator
 *     Parallel calculation of the confusion matrix for a window of streamed label prediction written in Scala
 *     https://github.com/SpyrosKou/StreamingConfusionMatrix
 *     Copyright (C) 2021  Spyros Koukas
 *
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
    val observationA = new ConfusionRow("A", Map("A" -> 124L, "B" -> 12L))
    val observationB = new ConfusionRow("B", Map("A" -> 11L, "B" -> 321L))

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

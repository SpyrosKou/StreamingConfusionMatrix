/**
 * StreamingConfusionMatrixCalculator
 * Parallel calculation of the confusion matrix for a window of streamed label prediction written in Scala
 * https://github.com/SpyrosKou/StreamingConfusionMatrix
 * Copyright (C) 2021  Spyros Koukas
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.streaming.model

import org.scalatest.flatspec._
import org.scalatest.matchers._

/**
 * Tests ModelsPredictionProbabilities
 *
 * @author Spyros Koukas
 */
class ModelsPredictionProbabilitiesTest extends AnyFlatSpec with should.Matchers {

  "A ModelsPredictionProbabilities" should "be created successfully for 2 models 2 labels" in {
    val probabilities = Map("model1" -> Map("A" -> 0.3, "B" -> 0.7),
      "model2" -> Map("A" -> 0.2, "B" -> 0.8))
    val id = 1
    val givenLabel = "A"

    val modelsPredictionProbabilities = new ModelsPredictionProbabilities(id, givenLabel, probabilities)
    modelsPredictionProbabilities.modelsToLabelsProbabilities.size should be(2)
    modelsPredictionProbabilities.modelsToLabelsProbabilities.get("model1").get.size should be(2)
    modelsPredictionProbabilities.modelsToLabelsProbabilities.get("model2").get.size should be(2)
  }

  "A ModelsPredictionProbabilities" should "be created successfully for 3 models 3 labels" in {
    val probabilities = Map("model1" -> Map("A" -> 0.3, "B" -> 0.5, "C" -> 0.2),
      "model2" -> Map("A" -> 0.2, "B" -> 0.5, "C" -> 0.3),
      "model3" -> Map("A" -> 0.1, "B" -> 0.2, "C" -> 0.7))
    val id = 1
    val givenLabel = "A"

    val modelsPredictionProbabilities = new ModelsPredictionProbabilities(id, givenLabel, probabilities)
    modelsPredictionProbabilities.modelsToLabelsProbabilities.size should be(3)
    modelsPredictionProbabilities.modelsToLabelsProbabilities.get("model1").get.size should be(3)
    modelsPredictionProbabilities.modelsToLabelsProbabilities.get("model2").get.size should be(3)
    modelsPredictionProbabilities.modelsToLabelsProbabilities.get("model3").get.size should be(3)
  }

  "A ModelsPredictionProbabilities" should "provide correct probabilities for 2 models and 2 labels" in {
    val probabilities = Map("model1" -> Map("A" -> 0.3, "B" -> 0.7),
      "model2" -> Map("A" -> 0.2, "B" -> 0.8))
    val id = 1
    val givenLabel = "A"

    val modelsPredictionProbabilities = new ModelsPredictionProbabilities(id, givenLabel, probabilities)
    modelsPredictionProbabilities.getModelLabelProbability("model1", "A") should be(0.3)
    modelsPredictionProbabilities.getModelLabelProbability("model1", "B") should be(0.7)
    modelsPredictionProbabilities.getModelLabelProbability("model2", "A") should be(0.2)
    modelsPredictionProbabilities.getModelLabelProbability("model2", "B") should be(0.8)

  }

  "A ModelsPredictionProbabilities" should "provide correct probabilities for 3 models and 3 labels" in {
    val probabilities = Map("model1" -> Map("A" -> 0.3, "B" -> 0.5, "C" -> 0.2),
      "model2" -> Map("A" -> 0.2, "B" -> 0.5, "C" -> 0.3),
      "model3" -> Map("A" -> 0.1, "B" -> 0.2, "C" -> 0.7))
    val id = 1
    val givenLabel = "A"

    val modelsPredictionProbabilities = new ModelsPredictionProbabilities(id, givenLabel, probabilities)
    //Model 1
    modelsPredictionProbabilities.getModelLabelProbability("model1", "A") should be(0.3)
    modelsPredictionProbabilities.getModelLabelProbability("model1", "B") should be(0.5)
    modelsPredictionProbabilities.getModelLabelProbability("model1", "C") should be(0.2)
    //Model 2
    modelsPredictionProbabilities.getModelLabelProbability("model2", "A") should be(0.2)
    modelsPredictionProbabilities.getModelLabelProbability("model2", "B") should be(0.5)
    modelsPredictionProbabilities.getModelLabelProbability("model2", "C") should be(0.3)
    //Model 3
    modelsPredictionProbabilities.getModelLabelProbability("model3", "A") should be(0.1)
    modelsPredictionProbabilities.getModelLabelProbability("model3", "B") should be(0.2)
    modelsPredictionProbabilities.getModelLabelProbability("model3", "C") should be(0.7)
  }

  "A ModelsPredictionProbabilities" should "calculate weighted observation correctly for 2 models and 2 labels " in {
    val probabilities = Map("model1" -> Map("A" -> 0.3, "B" -> 0.7),
      "model2" -> Map("A" -> 0.2, "B" -> 0.8))
    val id = 1
    val givenLabel = "A"

    val weights = Map("model1" -> 0.5, "model2" -> 0.6)
    //    1_A=0.15
    //    1_B=0.35
    //    2_A=0.12
    //    2_B=0.36
    // A=0.27
    // B=0.71
    // B>A

    val frequency = 1L
    val observationExpected = new Observation("A", Map("B" -> 1L))
    val modelsPredictionProbabilities = new ModelsPredictionProbabilities(id, givenLabel, probabilities)
    val observationResult = modelsPredictionProbabilities.observation(weights)
    observationResult.actualLabel should be(observationExpected.actualLabel)
    observationResult.observation should be(observationExpected.observation)
    observationResult.observation.get("B").get should be(observationExpected.observation.get("B").get)
    observationResult.observation.get("A") should be(observationExpected.observation.get("A"))
  }


}

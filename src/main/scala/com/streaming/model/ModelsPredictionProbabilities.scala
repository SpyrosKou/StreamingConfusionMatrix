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

/**
 * Created at 2021-04-26 on 22:09 
 *
 * @author Spyros Koukas
 */

/**
 * Models probability values of predictions from arbitrary many  machine learning models for a classification problem for arbitrary many labels (E.g. A, B, C)
 * Each model has an id e.g. Model_1, Model_2, Model_3 etc.
 *
 * @param id                          the id of the entry
 * @param givenLabel                  the given Label
 * @param modelsToLabelsProbabilities the
 */
final class ModelsPredictionProbabilities(val id: Long, val givenLabel: String, val modelsToLabelsProbabilities: Map[String, Map[String, Double]]) {

  /**
   * Will throw an AssertionError if the model_id does not exist or if the label requested does not exist for the given model_id
   *
   * @param model_id
   * @param label
   * @return the probability prediction
   */
  final def getModelLabelProbability(model_id: String, label: String): Double = {
    val modelPredictions = this.modelsToLabelsProbabilities.get(model_id)
    assert(modelPredictions.nonEmpty, "Model Id:[" + model_id + "] Not found")
    val probability = modelPredictions.get.get(label);
    assert(probability.nonEmpty, "Label:[" + label + "] for Model Id:[" + model_id + "] Not found")
    return probability.get;
    //Another more quiet alternative is to return 0 when a model or label for model does not exist
    //return this.modelsToLabelsProbabilities.getOrElse(model_id, Map.empty).getOrElse(label, 0.0)
  }

  /**
   *
   * @param weights
   * @return
   */
  final def observation(weights: Map[String, Double]): ConfusionRow = {
    var labelWeights = collection.mutable.Map.empty[String, Double]

    for ((modelId, modelProbabilities) <- this.modelsToLabelsProbabilities) {
      val modelWeightOption = weights.get(modelId)
      assert(modelWeightOption.nonEmpty, "Weight missing for modelId=[" + modelId + "]")
      val modelWeight = modelWeightOption.get

      for ((label, probability) <- modelProbabilities) {
        val newProbability = labelWeights.getOrElse(label, 0.0) + modelWeight * probability
        labelWeights.put(label, newProbability)
      }
    }
    // in case the labels get equal probability returns the second label, the
    val labelSelected = labelWeights.reduce(
      (a, b) => {
        if (a._2 > b._2) a
        else b
      }
    )._1
    //TODO, Observation can be simplified as it is always created with 1 predicted label
    return new ConfusionRow(this.givenLabel, Map {
      labelSelected -> 1L
    });
  }

  /**
   *
   * @return
   */
  override def toString = s"ModelsPredictionProbabilities(id=$id, givenLabel=$givenLabel, modelsToLabelsProbabilities=$modelsToLabelsProbabilities)"
}

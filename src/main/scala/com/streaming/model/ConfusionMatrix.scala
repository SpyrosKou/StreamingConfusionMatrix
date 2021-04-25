package com.streaming.model


import scala.language.postfixOps


/**
 * A generic confusion matrix, that can support any number of labels.
 * Each label is a String.
 * Created at 2021-04-25 on 10:05 
 *
 * @author Spyros Koukas
 */

final class ConfusionMatrix(val predictions: Map[String, Map[String, Long]]) {

  /**
   * Create an empty ConfusionMatrix
   */
  def this() {
    this(Map.empty)
  }

  /**
   * Add a new observation to Confusion Matrix
   *
   * @param observation
   */
  final def addPrediction(observation: Observation): ConfusionMatrix = {
    return addPrediction(observation.actualLabel, observation.observation)
  }

  /**
   * Remove an old observation from Confusion Matrix
   *
   * @param observation
   */
  final def removePrediction(observation: Observation): ConfusionMatrix = {
    return removePrediction(observation.actualLabel, observation.observation)
  }


  /**
   * Add a new prediction to Confusion Matrix
   *
   * @param actualLabel
   * @param prediction
   */
  final def addPrediction(actualLabel: String, prediction: scala.collection.Map[String, Long]): ConfusionMatrix = {
    return updatePrediction(actualLabel, prediction, mergePrediction)
  }

  /**
   * Remove an old prediction from Confusion Matrix
   *
   * @param actualLabel
   * @param oldPrediction
   */
  final def removePrediction(actualLabel: String, oldPrediction: scala.collection.Map[String, Long]): ConfusionMatrix = {
    return updatePrediction(actualLabel, oldPrediction, removePrediction)
  }

  /**
   *
   * @param actualLabel
   * @param prediction
   * @param function
   * @return
   */
  private final def updatePrediction(actualLabel: String,
                                     prediction: scala.collection.Map[String, Long],
                                     function: (scala.collection.Map[String, Long], scala.collection.Map[String, Long]) => scala.collection.immutable.Map[String, Long]): ConfusionMatrix = {
    val accumulatedPrediction = predictions.getOrElse(actualLabel, Map.empty[String, Long])
    val newPrediction = function(accumulatedPrediction, prediction)
    val newPredictions = predictions + (actualLabel -> newPrediction)
    val newConfusionMatrix = new ConfusionMatrix(newPredictions)
    return newConfusionMatrix
  }

  /**
   *
   * @param a non null Map
   * @param b non null Map
   * @return a new Map where new label counts have been added
   */
  private final def mergePrediction(a: scala.collection.Map[String, Long], b: scala.collection.Map[String, Long]): scala.collection.immutable.Map[String, Long] = {
    val allKeys = a.keys ++ b.keys
    (allKeys) map { key => key -> (a.getOrElse(key, 0L) + b.getOrElse(key, 0L)) } toMap
  }

  /**
   *
   * @param accumulated non null Map
   * @param dif         non null Map
   * @return a new Map where old label counts have been subtracted
   */
  private final def removePrediction(accumulated: scala.collection.Map[String, Long], dif: scala.collection.Map[String, Long]): scala.collection.immutable.Map[String, Long] = {
    val allKeys = accumulated.keys ++ dif.keys
    (allKeys) map { key => key -> (accumulated.getOrElse(key, 0L) - dif.getOrElse(key, 0L)) } toMap
  }

}

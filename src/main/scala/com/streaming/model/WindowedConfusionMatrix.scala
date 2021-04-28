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



import scala.collection.immutable.Queue



/**
 * A ConfusionMatrix that
 * - will maintain window size constant and valid by removing old values in a FIFO way
 * - Can fit any number of different labels
 * This implements a sliding window calculation of the ConfusionMatrix.
 * It could be implemented more in detail with Flow.sliding https://doc.akka.io/docs/akka/current/stream/operators/Source-or-Flow/sliding.html
 * Created at 2021-04-25 on 11:42 
 *
 * @author Spyros Koukas
 */
final class WindowedConfusionMatrix(val confusionMatrix: ConfusionMatrix, val windowPredictions: Int, val observations: Queue[Observation]) {

  def this(windowPredictions: Int) {
    this(new ConfusionMatrix(), windowPredictions, Queue.empty)
  }

  /**
   * The observation window is full when the observations are equal to the required windowPredictions
   *
   * @return
   */
  final def isWindowFull(): Boolean = {
    this.observations.length == this.windowPredictions
  }


  /**
   *
   * @param observation
   * @return
   */
  final def add(observation: Observation): WindowedConfusionMatrix = {

    //create a new queue that includes new observation
    var updatedQueue = this.observations.enqueue(observation)
    //create a new matrix, updated with new observation, this is a mutable reference
    var matrix = this.confusionMatrix.addPrediction(observation)

    //Iterate until the queue is in the required window size.
    // Should normally be executed once, but it may be handy for future functionalities.
    while (updatedQueue.length > this.windowPredictions) {
      val dequeue: (Observation, Queue[Observation]) = updatedQueue.dequeue
      //get the rest of the queue
      updatedQueue = dequeue._2
      //Remove from matrix
      matrix = matrix.removePrediction(dequeue._1)
    }

    return new WindowedConfusionMatrix(matrix, this.windowPredictions, updatedQueue)
  }

  /**
   *
   * @param actualLabel
   * @param resultLabel
   * @return
   */
  final def occurrences(actualLabel: String, resultLabel: String): Long = {
    val observationsForLabel = occurrences(actualLabel)

    return observationsForLabel.getOrElse(resultLabel, 0)
  }

  /**
   *
   * @param actualLabel
   * @return
   */
  final def occurrences(actualLabel: String): Map[String, Long] = {
    return this.confusionMatrix.predictions.getOrElse(actualLabel, Map.empty)

  }

  override def toString = s"WindowedConfusionMatrix(confusionMatrix=$confusionMatrix, windowPredictions=$windowPredictions, observations=$observations, isWindowFull=$isWindowFull)"
}

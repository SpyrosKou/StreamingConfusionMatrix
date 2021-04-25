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

/**
 * Created at 2021-04-25 on 12:12 
 *
 * @author Spyros Koukas
 */
final class Observation(val actualLabel: String, val observation: Map[String, Long]) {

  /**
   * Construct with empty Observation Map
   * @param actualLabel
   */
  def this(actualLabel: String) {
    this(actualLabel, Map.empty)
  }

}

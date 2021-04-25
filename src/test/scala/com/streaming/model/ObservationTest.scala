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

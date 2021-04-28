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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.io.Source
import akka.NotUsed
import akka.testkit._
import akka.stream._
import akka.stream.scaladsl._

/**
 *
 * Created at 2021-04-25 on 14:31 
 *
 * @author Spyros Koukas
 */
class WindowedConfusionMatrixTest extends AnyFlatSpec with should.Matchers {

  "A WindowedConfusionMatrix" should "be trivial to construct" in {
    val windowedConfusionMatrix = new WindowedConfusionMatrix(3)
    windowedConfusionMatrix.isWindowFull() should be(false)

  }

  "A WindowedConfusionMatrix" should "respect window limits" in {
    val observationA = new ConfusionRow("A", Map("A" -> 124L, "B" -> 12L))
    val observationB = new ConfusionRow("B", Map("A" -> 11L, "B" -> 321L))
    val observationC = new ConfusionRow("B", Map("A" -> 41L, "B" -> 231L))
    val observationD = new ConfusionRow("A", Map("A" -> 1224L, "B" -> 122L))

    //0
    val windowedConfusionMatrixA = new WindowedConfusionMatrix(3)
    windowedConfusionMatrixA.isWindowFull() should be(false)
    windowedConfusionMatrixA.occurrences("A", "A") should be(0L)
    windowedConfusionMatrixA.occurrences("A", "B") should be(0L)
    windowedConfusionMatrixA.occurrences("B", "A") should be(0L)
    windowedConfusionMatrixA.occurrences("B", "B") should be(0L)
    windowedConfusionMatrixA.occurrences("C", "A") should be(0L)
    windowedConfusionMatrixA.occurrences("C", "C") should be(0L)

    //1
    val windowedConfusionMatrixB = windowedConfusionMatrixA.add(observationA)
    windowedConfusionMatrixB.isWindowFull() should be(false)
    windowedConfusionMatrixB.occurrences("A", "A") should be(124L)
    windowedConfusionMatrixB.occurrences("A", "B") should be(12L)
    windowedConfusionMatrixB.occurrences("B", "A") should be(0L)
    windowedConfusionMatrixB.occurrences("B", "B") should be(0L)
    windowedConfusionMatrixB.occurrences("C", "A") should be(0L)
    windowedConfusionMatrixB.occurrences("C", "C") should be(0L)


    //2
    val windowedConfusionMatrixC = windowedConfusionMatrixB.add(observationB)
    windowedConfusionMatrixC.isWindowFull() should be(false)
    windowedConfusionMatrixC.occurrences("A", "A") should be(124L)
    windowedConfusionMatrixC.occurrences("A", "B") should be(12L)
    windowedConfusionMatrixC.occurrences("B", "A") should be(11L)
    windowedConfusionMatrixC.occurrences("B", "B") should be(321L)
    windowedConfusionMatrixC.occurrences("C", "A") should be(0L)
    windowedConfusionMatrixC.occurrences("C", "C") should be(0L)


    //3
    val windowedConfusionMatrixD = windowedConfusionMatrixC.add(observationC)
    windowedConfusionMatrixD.isWindowFull() should be(true)
    windowedConfusionMatrixD.occurrences("A", "A") should be(124L)
    windowedConfusionMatrixD.occurrences("A", "B") should be(12L)
    windowedConfusionMatrixD.occurrences("B", "A") should be(52L)
    windowedConfusionMatrixD.occurrences("B", "B") should be(552L)
    windowedConfusionMatrixD.occurrences("C", "A") should be(0L)
    windowedConfusionMatrixD.occurrences("C", "C") should be(0L)


    //4
    val windowedConfusionMatrixE = windowedConfusionMatrixD.add(observationD)
    windowedConfusionMatrixE.isWindowFull() should be(true)
    windowedConfusionMatrixE.occurrences("A", "A") should be(1224L)
    windowedConfusionMatrixE.occurrences("A", "B") should be(122L)
    windowedConfusionMatrixE.occurrences("B", "A") should be(52L)
    windowedConfusionMatrixE.occurrences("B", "B") should be(552L)
    windowedConfusionMatrixE.occurrences("C", "A") should be(0L)
    windowedConfusionMatrixE.occurrences("C", "C") should be(0L)

  }



}

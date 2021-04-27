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
package com.app

import org.scalatest.flatspec._
import org.scalatest.matchers._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Test the application as a whole
 *
 * @author Spyros Koukas
 */
class StreamingSlidingWindowConfusionMatrixTest extends AnyFlatSpec with should.Matchers {


  "The SlidingWindowConfusionMatrix" should " have correct number of window calculations and throughput" in {
    val windowSize = 1000
    val valuesInDatabase = 100_000
    val expectedWindows = valuesInDatabase - windowSize + 1
    val throughPutLimitRequirementPerSecond = 100

    val timeStart = System.currentTimeMillis()

    val streamingSlidingWindowConfusionMatrix = new StreamingSlidingWindowConfusionMatrix()

    val countFuture: Future[Long] = streamingSlidingWindowConfusionMatrix.calculate();


    //Wait until test is done
    val counter = Await.result(countFuture, Duration.Inf)

    val measuredThroughput = calculateThroughput(timeStart, timeFinish = System.currentTimeMillis(), valuesInDatabase)
    println("measuredThroughput:[" + measuredThroughput + "] values per second")

    streamingSlidingWindowConfusionMatrix.close()
    //Half of the times the given label was A and the prediction was B
    counter should be(expectedWindows)
    //there should be a more formal way to express that,
    val throughputOK = measuredThroughput >= throughPutLimitRequirementPerSecond
    throughputOK should be(true)


  }

  /**
   *
   * @param timeStart
   * @param timeFinish
   * @param measurements
   */
  private final def calculateThroughput(timeStart: Long, timeFinish: Long, measurements: Long): Long = {
    val timeFinish = System.currentTimeMillis()
    val durationMillis = (timeFinish - timeStart) / 1000
    val measuredThroughput = measurements / durationMillis
    return measuredThroughput
  }

}

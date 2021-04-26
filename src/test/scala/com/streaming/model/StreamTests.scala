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

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Created at 2021-04-25 on 14:31 
 *
 * @author Spyros Koukas
 */
final class StreamTests extends AnyFlatSpec with should.Matchers {


  "A WindowedConfusionMatrix in a Stream" should " observe 1001 full windows in a 2000 observation sequence" in {
    implicit val system: ActorSystem = ActorSystem("Test")
    val windowSize = 1000
    val observationSize = 2000
    val expectedWindows = 1001


    val countFuture: Future[Int] = Source(1 to observationSize).
      //Create observationSize different observations
      map(dummyObservationCreator).
      //and run them on windowed confusion matrix
      scan(new WindowedConfusionMatrix(windowSize))((window, observation) => {
        //                println(observation.actualLabel + " - " + observation.observation.get("A"))
        window.add(observation)
      }
      ).

      //filter only full windows
      filter(window => window.isWindowFull()).
      scan(0)((count, window) => {
        val tempCount = count + 1
        println("counter:" + tempCount)
        count + 1
      }).
      //get the count by reducing, there should  be a  more efficient way
      runReduce((x,y)=>x max y)


    implicit val ec = system.dispatcher

    countFuture.onComplete(totalValues => {
      //terminate actor system
      system.terminate()

    })

    //Wait until test is done
    val counter = Await.result(countFuture, Duration.Inf)
    counter should be((expectedWindows))

  }

  /**
   *
   * @param i
   * @return
   */
  private final def dummyObservationCreator(i: Int): Observation = {
    val label = if (i % 2 == 0) "A" else "B"
    return new Observation(label, Map("A" -> i * 2L, "B" -> i * 1L))
  }

}

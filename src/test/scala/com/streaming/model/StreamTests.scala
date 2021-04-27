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
 *
 * These are a set of tests related to the usage of the model in an akka stream
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
      // enumerate
      map(_ => 1).
      //count by reduction
      runReduce((x, y) => x + y)


    implicit val ec = system.dispatcher

    countFuture.onComplete(totalValues => {
      //terminate actor system
      system.terminate()

    })

    //Wait until test is done
    val counter = Await.result(countFuture, Duration.Inf)
    counter should be((expectedWindows))

  }


  "A ModelsPredictionProbabilities in a Stream" should " predict the correct label" in {
    implicit val system: ActorSystem = ActorSystem("Test")
    val windowSize = 1000
    val expectedWindows = 1001
    val observationSize = 2000

    val weights = Map("model1" -> 0.5, "model2" -> 0.6)


    val countFuture: Future[(Long,Long,Long,Long,Int)] = Source(1 to observationSize).
      //Create ModelsPredictionProbabilities that all predict B, but have alternate given Labels
      map(dummyModelsPredictionProbabilities).
      //and run them on windowed confusion matrix
      map(dummyModelsPredictionProbabilities => dummyModelsPredictionProbabilities.observation(weights)).

      //and run them on windowed confusion matrix
      scan(new WindowedConfusionMatrix(windowSize))((window, observation) => {
        //                println(observation.actualLabel + " - " + observation.observation.get("A"))
        window.add(observation)
      }
      ).

      //filter only full windows
      filter(window => window.isWindowFull()).

      map(window => {
        //a tupple with results of AB, BB, AA, BA
        (window.occurrences("A", "B"),
          window.occurrences("B", "B"),
          window.occurrences("A", "A"),
          window.occurrences("B", "A"),
        1)
      }).
      //count by reduction
      runReduce((x, y) => (x._1 + y._1, x._2 + y._2, x._3 + y._3, x._4 + y._4,x._5 + y._5))


    implicit val ec = system.dispatcher

    countFuture.onComplete(totalValues => {
      //terminate actor system
      system.terminate()

    })

    //Wait until test is done
    val counter = Await.result(countFuture, Duration.Inf)
    //Half of the times the given label was A and the prediction was B
    counter._1 should be(expectedWindows * windowSize/2)
    //Half of the times the given label was B and the prediction was B
    counter._2 should be(expectedWindows * windowSize/2)
    //There should not be any A prediction
    counter._3 should be(0L)
    //There should not be any A prediction
    counter._4 should be(0L)

    //Expected full windows observed
    counter._5 should be(expectedWindows)
  }


  "An end to end stream" should " predict the correct labels and maintain proper window size" in {
    implicit val system: ActorSystem = ActorSystem("Test")

    val observationSize = 2000
    val weights = Map("model1" -> 0.5, "model2" -> 0.6)


    val countFuture: Future[Int] = Source(1 to observationSize).
      //Create ModelsPredictionProbabilities that all predict B, but have alternate given Labels
      map(dummyModelsPredictionProbabilities).
      //and run them on windowed confusion matrix
      map(dummyModelsPredictionProbabilities => dummyModelsPredictionProbabilities.observation(weights)).
      //filter only B predicted observations
      filter(observation => observation.observation.get("B").nonEmpty).
      // enumerate
      map(_ => 1).
      //count by reduction
      runReduce((x, y) => x + y)


    implicit val ec = system.dispatcher

    countFuture.onComplete(totalValues => {
      //terminate actor system
      system.terminate()

    })

    //Wait until test is done
    val counter = Await.result(countFuture, Duration.Inf)
    counter should be((observationSize))

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

  /**
   * Generate a dummy model, where A is always the predicted model, while the given model is created based on the value of the input:
   * - A if i is even
   * - B if i is odd
   *
   * @param i is the id of the input, and also "controls" the given Label
   * @return
   */
  private final def dummyModelsPredictionProbabilities(i: Int): ModelsPredictionProbabilities = {
    val probabilities = Map(
      "model1" -> Map("A" -> 0.3, "B" -> 0.7),
      "model2" -> Map("A" -> 0.2, "B" -> 0.8)
    )

    val givenLabel = if (i % 2 == 0) "A" else "B"

    val modelsPredictionProbabilities = new ModelsPredictionProbabilities(i, givenLabel, probabilities)
    return modelsPredictionProbabilities
  }

}

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
 * Configurations for project
 * Created at 2021-04-27 on 21:29 
 *
 * @author Spyros Koukas
 */
final object Configuration {

  /**
   * Hardcoded weights for 3 models
   */
  val WEIGHTS_MAP = Map("model1" -> 0.5, "model2" -> 0.6, "model3" -> 0.7)

  val WINDOW_SIZE = 1000

  final object Calculations{
    val SUB_STREAMS = 4
    val BATCH_WRITES=100
  }

  /**
   * Hardcoded Configuration about the ElasticSearchClient
   */
  final object ElasticSearchClient {
    val INPUT_INDEX_NAME = "raw_data_input"
    val OUTPUT_INDEX_NAME = "calculated_confusion_matrix"
    val IP = "localhost"
    val PORT = 9200
    val SCHEME = "http"
  }
}

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
package com.elasticsearch.query

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.streaming.model.{ConfusionMatrix, ModelsPredictionProbabilities}
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.{RequestOptions, RestHighLevelClient}
import org.elasticsearch.common.xcontent.{XContentBuilder, XContentFactory}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

import java.util


/**
 * Created at 2021-04-28 on 00:39
 *
 * @author Spyros Koukas
 */

/**
 *  Converts object to the appropriate format for JSON/ElasticSearch storage
 *
 */
final class JsonParser {

  /**
   * Convert a ConfusionMatrix to
 *
   * @param confusionMatrix
   * @return
   */
  final def toJsonXContentBuilder(confusionMatrix: ConfusionMatrix): XContentBuilder = {
    var xContentBuilder: XContentBuilder = XContentFactory.
      jsonBuilder().startObject()

    for ((givenLabel, labelValueMap) <- confusionMatrix.predictions) {
      xContentBuilder = xContentBuilder.startObject(givenLabel)
      for ((label, labelCount) <- labelValueMap) {
        xContentBuilder = xContentBuilder.field(label, labelCount)
      }
      xContentBuilder = xContentBuilder.endObject()
    }
    xContentBuilder = xContentBuilder.endObject()
    return xContentBuilder
  }


}

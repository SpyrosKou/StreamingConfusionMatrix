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

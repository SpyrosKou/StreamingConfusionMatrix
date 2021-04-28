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
import com.streaming.model.ModelsPredictionProbabilities
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.{RequestOptions, RestHighLevelClient}
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder


/**
 * Created at 2021-04-27 on 19:39
 *
 * @author Spyros Koukas
 */

/**
 * Fetches ModelPrediction data from a given ElasticSearch index
 * Does not open, or close client, only uses the client.
 *
 * @param client a non null, already connected client is expected. The caller is responsible to open/close connection
 * @param index  the index to get the data from
 */
final class PersistenceAccess(client: RestHighLevelClient, index: String) {
  /**
   * Assume that there is a known first element id
   */
  private val FIRST_ELEMENT_ID = 1L;

  /**
   * This function assumes elements start at id
   * This could be also implemented with Source.unfoldResource
   */
  final def getAllInputsSource(): Source[ModelsPredictionProbabilities, NotUsed] = {
    val firstElement = get(FIRST_ELEMENT_ID).get
    val source = Source.unfold(firstElement) { currentElement =>
      val nextElement = getNext(currentElement)
      if (currentElement == null) None
      else Some(nextElement.getOrElse(null), currentElement)
    }
    return source
  }

  /**
   * Search for a single result with a specified id
   *
   * @param id
   */
  final def get(id: Long): Option[ModelsPredictionProbabilities] = {

    val searchRequest = new SearchRequest(index)

    val sourceBuilder: SearchSourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(QueryBuilders.termQuery("id", id));

    searchRequest.source(sourceBuilder)
    val searchResult = client.search(searchRequest, RequestOptions.DEFAULT)

    //    println("SearchResult " + searchResult.getHits)
    val resultHits = searchResult.getHits.getHits

    assert(resultHits.length <= 1, "Search returned more than one raw data entries with the same ID")

    if (resultHits.isEmpty) {
      return None
    } else {
      //assume only one result
      val resultHit = resultHits.head

      //Convert to model
      val resultsMap = resultHit.getSourceAsMap
      val resultModelled = convertToModelsPredictionProbabilities(resultsMap)

      return Some(resultModelled)
    }
  }

  /**
   *
   * @param current
   * @return the next value if available
   */
  private final def getNext(current: ModelsPredictionProbabilities): Option[ModelsPredictionProbabilities] = {
    if (current == null) {
      None
    }
    else {
      get(current.id + 1)
    }
  }


  /**
   * Create a {@Link ModelsPredictionProbabilities} from a search query hit result
   * This method assumes all expected values are present and does not handle dirty or incomplete data.
   *
   * @param resultsMap
   * @return
   */
  private final def convertToModelsPredictionProbabilities(resultsMap: java.util.Map[String, AnyRef]): ModelsPredictionProbabilities = {

    val resultId: Long = resultsMap.get("id").toString.toLong
    val givenLabel: String = resultsMap.get("given_label").toString

    val model1_A: Double = resultsMap.get("model1_A").toString.toDouble
    val model1_B: Double = resultsMap.get("model1_B").toString.toDouble

    val model2_A: Double = resultsMap.get("model2_A").toString.toDouble
    val model2_B: Double = resultsMap.get("model2_B").toString.toDouble

    val model3_A: Double = resultsMap.get("model3_A").toString.toDouble
    val model3_B: Double = resultsMap.get("model3_B").toString.toDouble

    val modelsToLabelsProbabilities: Map[String, Map[String, Double]] =
      Map(
        "model1" -> Map("A" -> model1_A, "B" -> model1_B),
        "model2" -> Map("A" -> model2_A, "B" -> model2_B),
        "model3" -> Map("A" -> model3_A, "B" -> model3_B),
      )

    val resultModelled = new ModelsPredictionProbabilities(resultId, givenLabel, modelsToLabelsProbabilities)

    return resultModelled
  }

  /**
   * Persist the xContentBuilder in the outputIndexName of the connected ElasticSearch
   *
   * @param xContentBuilder
   * @param outputIndexName
   * @return true if successfully saved
   */
  final def put(xContentBuilder: XContentBuilder, outputIndexName: String): Boolean = {
    return put(List(xContentBuilder), outputIndexName)
  }

  /**
   * Persist a list of xContentBuilder in the outputIndexName of the connected ElasticSearch
   *
   * @param xContentBuilder
   * @param outputIndexName
   * @return true if successfully saved
   */
  final def put(xContentBuilders: List[XContentBuilder], outputIndexName: String): Boolean = {
    val bulkRequest = new BulkRequest(outputIndexName)

    xContentBuilders.foreach( xContentBuilder => {
      val indexRequest: IndexRequest = new IndexRequest(outputIndexName)
      indexRequest.source(xContentBuilder)
      bulkRequest.add(indexRequest)
    })


    val response = client.bulk(bulkRequest, RequestOptions.DEFAULT)
    // println("Response:" + response.getResult)
    return !response.getItems.toList.exists(item => item.isFailed)
  }


}

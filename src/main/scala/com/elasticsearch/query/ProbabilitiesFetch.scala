package com.elasticsearch.query

import com.streaming.model.ModelsPredictionProbabilities
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.{RequestOptions, RestHighLevelClient}
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
final class ProbabilitiesFetch(client: RestHighLevelClient, index: String) {

  /**
   * Search for a single result with a specified id
   *
   * @param id
   */
  final def get(id: Long): ModelsPredictionProbabilities = {

    val searchRequest = new SearchRequest(index)

    val sourceBuilder: SearchSourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(QueryBuilders.termQuery("id", id));

    searchRequest.source(sourceBuilder)
    val searchResult = client.search(searchRequest, RequestOptions.DEFAULT)

    //    println("SearchResult " + searchResult.getHits)
    val resultHits = searchResult.getHits.getHits

    assert(resultHits.length <= 1, "Search returned more than one raw data entries with the same ID")

    //assume only one result
    val resultHit = resultHits.head

    //Convert to model
    val resultsMap = resultHit.getSourceAsMap
    val resultModelled = resultHitToModelsPredictionProbabilities(resultsMap)

    return resultModelled
  }

  /**
   * Create a {@Link ModelsPredictionProbabilities} from a search query hit result
   * This method assumes all expected values are present and does not handle dirty or incomplete data.
   *
   * @param resultsMap
   * @return
   */
  private final def resultHitToModelsPredictionProbabilities(resultsMap: java.util.Map[String, AnyRef]): ModelsPredictionProbabilities = {

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

}

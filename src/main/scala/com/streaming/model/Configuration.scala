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

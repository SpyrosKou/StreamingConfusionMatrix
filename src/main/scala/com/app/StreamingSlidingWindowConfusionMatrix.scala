package com.app

import com.streaming.model.Configuration.ElasticSearchClient._
import org.apache.http.HttpHost
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}

final object StreamingSlidingWindowConfusionMatrix {
  final def main(args: Array[String]) = {
    val appInstance = new StreamingSlidingWindowConfusionMatrix()


    println(INPUT_INDEX_NAME + " index exists: " + appInstance.indexExists(INPUT_INDEX_NAME))
    println(OUTPUT_INDEX_NAME + " index exists: " + appInstance.indexExists(OUTPUT_INDEX_NAME))

    //Calculates Streaming instances.
    appInstance.calculate()

    appInstance.close()
  }
}


final class StreamingSlidingWindowConfusionMatrix {


  private val client = new RestHighLevelClient(RestClient.builder(new HttpHost(IP, PORT, SCHEME)))

  /**
   *
   * @return the number of inputs read, outputs processed,millis duration
   */
  final def calculate(): (Long, Long, Long) = {
    (1L, 1L, 1L)
  }

  /**
   *
   * @param name
   * @return
   */
  private final def indexExists(name: String): Boolean = {
    val request = new GetIndexRequest(name);
    client.indices().exists(request, RequestOptions.DEFAULT)
  }


  final def close() = {
    client.close()
  }
}

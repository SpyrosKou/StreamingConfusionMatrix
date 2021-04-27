package com.app

import akka.actor.ActorSystem
import com.elasticsearch.query.{JsonParser, PersistenceAccess}
import com.streaming.model.Configuration.ElasticSearchClient._
import com.streaming.model.Configuration.{ElasticSearchClient, SUB_STREAMS}
import com.streaming.model.{Configuration, WindowedConfusionMatrix}
import org.apache.http.HttpHost
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

final object StreamingSlidingWindowConfusionMatrix {
  final def main(args: Array[String]) = {
    val appInstance = new StreamingSlidingWindowConfusionMatrix()


    println(INPUT_INDEX_NAME + " index exists: " + appInstance.indexExists(INPUT_INDEX_NAME))
    println(OUTPUT_INDEX_NAME + " index exists: " + appInstance.indexExists(OUTPUT_INDEX_NAME))

    //Calculates Streaming instances.
    val countFuture = appInstance.calculate()
    //Wait until test is done
    val counter = Await.result(countFuture, Duration.Inf)

    appInstance.close()
  }
}


final class StreamingSlidingWindowConfusionMatrix {


  private val client = new RestHighLevelClient(RestClient.builder(new HttpHost(IP, PORT, SCHEME)))

  /**
   *
   * @return a future that returns the number of inputs processed when the process is complete
   */
  final def calculate(): Future[Long] = {
    implicit val system: ActorSystem = ActorSystem("Test")

    val weights = Configuration.WEIGHTS_MAP

    val client = new RestHighLevelClient(RestClient.builder(new HttpHost(ElasticSearchClient.IP, ElasticSearchClient.PORT, ElasticSearchClient.SCHEME)))
    val jsonParser: JsonParser = new JsonParser();
    val persistenceAccess = new PersistenceAccess(client, ElasticSearchClient.INPUT_INDEX_NAME)
    val timeStart = System.currentTimeMillis()
    val countFuture: Future[Long] = persistenceAccess.getAllInputsSource().
      async.
      //and run them on windowed confusion matrix
      map(modelsPredictionProbabilities => modelsPredictionProbabilities.observation(weights)).

      //and run them on windowed confusion matrix
      scan((new WindowedConfusionMatrix(Configuration.WINDOW_SIZE), 0))((windowTuple, observation) => (windowTuple._1.add(observation), windowTuple._2 + 1)).

      //filter only full windows
      filter(windowTuple => windowTuple._1.isWindowFull()).
      async.
      groupBy(SUB_STREAMS, tuple => tuple._2 % SUB_STREAMS).
      map(x => x._1).
      map(windowTuple => windowTuple.confusionMatrix).
      map(confusionMatrixTuple => jsonParser.toJsonXContentBuilder(confusionMatrixTuple)).
      //count successes
      map(json => persistenceAccess.put(json, Configuration.ElasticSearchClient.OUTPUT_INDEX_NAME)).
      async.
      map(result => if (result) 1L else 0L).

      mergeSubstreams.
      //      map(result => 1L).
      //count by reduction
      runReduce((x, y) => x + y)


    implicit val ec = system.dispatcher

    countFuture.onComplete(totalValues => {
      //terminate actor system
      system.terminate()
    })


    return countFuture

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

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

import akka.protobufv3.internal.compiler.PluginProtos.CodeGeneratorResponse.File
import com.streaming.model.Configuration.ElasticSearchClient
import org.apache.http.HttpHost
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import org.scalatest.flatspec._
import org.scalatest.matchers._

import java.io
import java.nio.file.Files

/**
 * PersistenceAccess related functionality
 *
 * @author Spyros Koukas
 */
class PersistenceAccessTest extends AnyFlatSpec with should.Matchers {

  "PersistenceAccess" should "be able to store results in a file" in {

    val client = new RestHighLevelClient(RestClient.builder(new HttpHost(ElasticSearchClient.IP, ElasticSearchClient.PORT, ElasticSearchClient.SCHEME)))

    val persistenceAccess = new PersistenceAccess(client, ElasticSearchClient.INPUT_INDEX_NAME)
    val directory="target/tests/com/elasticsearch/query/PersistenceAccessTest/"
    val directoryFile=new io.File(directory)
    directoryFile.mkdirs() should  be(true)
    persistenceAccess.getAllSavedOutputsToFile(ElasticSearchClient.OUTPUT_INDEX_NAME, (directory+"Results.json")) should be(true)
    client.close()

  }

}

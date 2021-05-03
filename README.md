# StreamingConfusionMatrix
Parallel calculation of the confusion matrix for a sliding window of streamed label predictions written in Scala.
Supports an arbitrary number of models.

## Confusion Matrix
A confusion matrix, is a specific table layout that captures the performance of a supervised statistical classification algorithm. [Confusion Matrix - Wikipedia](https://en.wikipedia.org/wiki/Confusion_matrix).


[The StreamingConfusionMatrix](https://github.com/SpyrosKou/StreamingConfusionMatrix) project is a [scala](https://scala-lang.org/) implementation that uses of [AKKA Streams](https://doc.akka.io/docs/akka/current/stream/stream-introduction.html) to calculate a confusion matrix for different models over a specifid observation window againist a potentially infinite stream of classification observations. The calculation can be parameterized for any number of classification labels, any size of window as well as any number of models.

## Calculation Process
The calculation process has been optimized for throughput at the cost of latency.
The following are the key optimizations in a high level, flow view of the calculation process. Another optimization is the calculation of the sliding window which is described in [Sliding Window Implementation](#sliding-window-implementation)
A high level design of the flow is provided as a diagram with **hyperlinks** to source level in github.
<img src="https://raw.githubusercontent.com/SpyrosKou/StreamingConfusionMatrix/main/FlowDiagram.svg">

### Async Pipelining
The calculation is implemented as a flow of calculation steps that starts from a Source and is completed in a sequence of transformation (mapping) steps.
Some steps are implemented asynchronously, this allows the calculation steps to be calculated in parallel in pipeline, increasing the performance.
This async calculation introduces both some CPU overhead and memory overhead, as calculations are passed from one stage to the other and because internal buffers are used to store the data, so it has not been added in very simple steps. 

### Batches
While, testing this project the main bottleneck wash the writing to the database one Confusion Matrix at a time using `IndexRequests`. 
Using `BatchRequests` was used to increase the writing throughput. This required some batching of the writes that is handled using the [batch](https://doc.akka.io/docs/akka/current/stream/operators/Source-or-Flow/batch.html) operator.
The [configuration class](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/Configuration.scala) has a `Calculations.BATCH_WRITES` constant that controls the size of the maximum batch.
Batching increases the required memory and latency, however it can improve throughput.
The way the batching is implemented utilizes the asynchronous concurrent pipelined calculations and backpressure. In particular:
 - the upstream flow will wait once the batching buffer is full, this is an effect of backpressure
 - the downstream flow (that writes the data to database) will take all available data to save them even if they are less than the current limit
 - the downstream flow only takes data when they are available (no need to handle a potential lack of data) 

### Parallel Computation 
Splitting the main flow in multiple flows was also used to increase performance. This is done after the computation of the Sliding Window whose current algorithm requires a sequential flow of the data.
It would be theoretically possible to further exploit such optimizations by e.g. split the computations preceding the Sliding Window calculation into substreams and merge them before computing the Sliding Window.
The [configuration class](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/Configuration.scala) has a `Calculations.SUB_STREAMS` constant that controls the number of substreams that can be created.
Setting `Calculations.SUB_STREAMS=1` is logically equivalent to using a linear process. This could be used if writing data to the database sequentially is required, without changing the current implementation.


## Sliding Window Implementation

A [Windowed Confusion Matrix](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/WindowedConfusionMatrix.scala) implements a window
This is implemented by using a [ConfusionMatrix](#confusion-matrix) and a FIFO queue. The WindowedConfusionMatrix should be initialized with a specific window size.
The class has been designed in a way that it is possible to create new instances of different window sizes, however this was not tested.
A window of N elements introduces a latency of (N-1) elements , as the first N-1 elements will be consumed, but the resulting window will not be valid.
The consumer may check if the window is full by calling the `isWindowFull()`
Once the window is full the object will maintain the window size constant removing old values in a FIFO way.
The queue is used to keep track of the elements history and perform only the required calculations on the results.

Another notable implementation approach would have been to use  [sliding](https://doc.akka.io/docs/akka/current/stream/operators/Source-or-Flow/sliding.html)

## Elastic Search access

### Configuration
This project requires an Elastic Search instance running on localhost at the default port.
Two indexes are accessed, one for reading data. The hardcoded configuration can be changed from the [configuration class](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/Configuration.scala).
All access to Elastic Search is handled by the [PersistenceAccess](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/elasticsearch/query/PersistenceAccess.scala)

### Data Conversion

#### Input Reading
A [Source](https://doc.akka.io/api/akka/2.6/akka/stream/scaladsl/Source.html) is created in [PersistenceAccess](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/elasticsearch/query/PersistenceAccess.scala) that reads input one-by-one and converts it to the [Models Probability Prediction](#Models-Probabilities-Prediction) model.
The Elastic Search access provided all functionality needed to access the saved data in the form of an attribute/value map.
The conversion from the persisted attribute-value form to [Models Probability Prediction](#models-probabilities-prediction) takes place in [PersistenceAccess](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/elasticsearch/query/PersistenceAccess.scala)

#### Output Saving
The libraries provided together with Elastic Search have been used to convert each Confusion Matrix calculated to JSON. The related class is [JsonParser](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/elasticsearch/query/JsonParser.scala).
The writing takes place in batches of multiple confusion matrixes at one BatchRequest.

## Models

### Models Probabilities Prediction
This is the input of the processing.
A [Models Probability Prediction](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/ModelsProbabilitiesPrediction.scala) models the input data from external systems
A ModelsProbabilitiesPrediction is an entry with a specific id and a single given label.  ModelsProbabilitiesPrediction contains  probability values of from arbitrary many machine learning models, for a classification problem for arbitrary many labels (E.g. A, B, C)
Each model has an id e.g. Model_1, Model_2, Model_3 etc. This is modelled as a `Map[String,Map[Strin,Double]]` where the first key is the model id and the result is a map from the labels to the predictions probabilities.

### Confusion Matrix
This is the output of the processing.
A [Confusion Matrix ](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/ConfusionMatrix.scala) models the output of the calculation.
The model favours re-usability and flexibility allowing for arbitrary many, different models and labels.
It is a wrapper around a `Map[String,Map[String,Long]]`, which maps a given label to a `Map[String,Long]`, a map of labels to predictions.
`Long` has been chosen to allow more than `Int` values during processing.

### Confusion Row
A [confusion row](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/ConfusionRow.scala) is simplified [confusion matrix](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/ConfusionMatrix.scala).
A [confusion row](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/ConfusionRow.scala) only contains information for a single given label.
Following the choice in the Confusion Matrix modelling, `Long` has been chosen to allow more than `Int` values. 

### Weights
This projects assumes a constant weights per model. So weights are stored as a map from modelId to weight values. The current instance contains a hardcoded configuration of 3 models in the [configuration class](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/Configuration.scala).


## Motivation
This is a demo project that uses [scala](https://scala-lang.org/) for streaming computation.
The [configuration class](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/main/scala/com/streaming/model/Configuration.scala) allows the user to configure how many substreams are created and how large batches are saved.
Some provided tests measure and report the throughput. 

## Tests
 - [ConfusionRowTest](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/test/scala/com/streaming/model/ConfusionRowTest.scala),[ModelsProbabilitiesPredictionTest](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/test/scala/com/streaming/model/ModelsProbabilitiesPredictionTest.scala) and [ConfusionMatrixTest](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/test/scala/com/streaming/model/ConfusionMatrixTest.scala) contain basics tests and calculation for the related models
 - The correctness of the [sliding window Implementation](#sliding-window-implementation), is tested in [WindowedConfusionMatrixTest](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/test/scala/com/streaming/model/WindowedConfusionMatrixTest.scala)
 - [StreamTests](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/test/scala/com/streaming/model/StreamTests.scala) contains tests related to certain scenarios of the project components, and allows for some experimentation
 - [StreamingSlidingWindowConfusionMatrixTest.scala](https://github.com/SpyrosKou/StreamingConfusionMatrix/blob/main/src/test/scala/com/app/StreamingSlidingWindowConfusionMatrixTest.scala) tests the throughput of the App. It also makes a basic check of the calculation by testing the number of produced windows. 



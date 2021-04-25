# StreamingConfusionMatrix
Streaming parallel calculation of a windowed confusion matrix for label prediction of N different models concurrently.

## Confusion Matrix
A confusion matrix, is a specific table layout that visualizes the performance of a supervised statistical classification algorithm. [Confusion Matrix - Wikipedia](https://en.wikipedia.org/wiki/Confusion_matrix)

[The StreamingConfusionMatrix](https://github.com/SpyrosKou/StreamingConfusionMatrix) project is a [scala](https://scala-lang.org/) implementation that uses of [AKKA Streams](https://doc.akka.io/docs/akka/current/stream/stream-introduction.html) to calculate a confusion matrix for different models over a specifid observation window againist a potentially infinite stream of classification observations. The calculation can be parameterized for any number of classification labels, any size of window as well as any number of models.

## Motivation
The main motivation for the creation of this project is to play using [scala](https://scala-lang.org/)  
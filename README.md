# StreamingConfusionMatrix
Streaming parallel calculation of a windowed confusion matrix for label prediction of N different models concurrently.

A confusion matrix, is a specific table layout that visualizes the performance of a supervised statistical classification algorithm. [Confusion Matrix - Wikipedia](https://en.wikipedia.org/wiki/Confusion_matrix)

[The StreamingConfusionMatrix](https://github.com/SpyrosKou/StreamingConfusionMatrix) project is a [scala](https://scala-lang.org/) implementaiton that uses of [AKKA Streams](https://doc.akka.io/docs/akka/current/stream/stream-introduction.html) to calculate a Windowed confusion matrix for different models. The calculation can be parameterized for any number of classification labels, any size of window as well as any number of models.

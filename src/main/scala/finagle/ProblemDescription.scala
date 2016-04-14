package finagle

import java.util.concurrent.atomic.AtomicInteger

import com.twitter.finagle._
import com.twitter.util._

object ProblemDescription {

  def main(args: Array[String]): Unit = {
    // Make sure nothing runs here, connection refused works fine for reproducing the problem
    val host = "localhost"
    val port = 8080

    (1 to 10).foreach { i =>
      println(s"Starting client run $i...")
      Await.ready(createClientAndRun(host, port, 1))
    }
  }

  def createClientAndRun(host: String, port: Int, iterations: Int): Future[Unit] = {
    val client = Http.newService(s"$host:$port")
    val remaining = new AtomicInteger(iterations)

    def invoke(): Future[Unit] = {
      if (remaining.decrementAndGet() == 0) Future.value(Unit)
      else {
        val request = http.Request(http.Method.Get, "/")
        request.host = host
        client(request)
      }
    }.transform {
      case Return(resp) =>
        invoke()
      case Throw(ex) =>
        Future.exception(ex)
    }
    invoke()
  }
}

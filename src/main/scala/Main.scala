package io.github.pjfanning.pekko.http

import kong.unirest.core.Unirest
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import org.apache.pekko.http.scaladsl.Http

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.DurationInt

object Main extends App with Directives {

  implicit val system: ActorSystem = ActorSystem("pekko-http-sample")
  sys.addShutdownHook(system.terminate())
  implicit val executionContext: ExecutionContext = system.dispatcher

  val bindingFuture = Http().newServerAt("0.0.0.0", 8080)
      .bind(hello)

  val binding = Await.result(bindingFuture, 10.seconds)

  println("Server online at http://localhost:8080/")

  val req = Unirest.get("http://localhost:8080/hello")
  println("response1 " + req.asString().getBody)

  val bindingEvent = Await.result(binding.terminate(1.minute), 1.minute)
  println("terminateEvent " + bindingEvent)

  println("response2 " + req.asString().getBody)

  //Thread.sleep(1000000)

  def hello: Route =
    path("hello") {
      get {
        complete {
          "hello"
        }
      }
    }
}
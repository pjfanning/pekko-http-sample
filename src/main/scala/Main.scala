package io.github.pjfanning.pekko.http

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import org.apache.pekko.http.scaladsl.{ConnectionContext, Http}

import java.nio.file.{Files, Paths}
import java.security.KeyStore
import javax.net.ssl.{KeyManagerFactory, SSLContext}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Using

object Main extends App with Directives {

  implicit val system: ActorSystem = ActorSystem("pekko-http-sample")
  sys.addShutdownHook(system.terminate())
  implicit val executionContext: ExecutionContext = system.dispatcher

  // keystore.jks is provided just for this demo - do not use it in production applications
  val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)
  val pwd = "changeit"
  Using.resource(Files.newInputStream(Paths.get("keystore.jks"))) { fos =>
    keyStore.load(fos, pwd.toCharArray)
  }
  val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
  keyManagerFactory.init(keyStore, pwd.toCharArray)

  val sslContext = SSLContext.getInstance("TLS")
  sslContext.init(keyManagerFactory.getKeyManagers, null, null)

  val f = for {
    bindingFuture <- Http().newServerAt("0.0.0.0", 8443)
      .enableHttps(ConnectionContext.httpsServer(sslContext))
      .bind(hello)
    waitOnFuture <- Future.never
  } yield waitOnFuture

  Await.ready(f, Duration.Inf)

  def hello: Route =
    path("hello") {
      get {
        complete {
          "hello"
        }
      }
    }
}
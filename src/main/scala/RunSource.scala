package sandbox

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Source}

import scala.concurrent.duration._
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

/*
* Provide a stream of 1..n to anyone opening a connection
*/
object TestRunSource extends FailFastCirceSupport with LazyLogging {

  def main(args: Array[String]): Unit = {
    val (interface, port: Int) = ("0.0.0.0", 8083)
    import as.dispatcher    // ExecutionContext

    val fut: Future[Http.ServerBinding] = Http().bindAndHandle( route, interface, port )

    logger.info( s"Service running at http://localhost:$port")
  }

  private
  val src: Source[Data,_] = Source(1 to Int.MaxValue)
    .map(Data(_))
    .throttle(300, 1 second)
    .map{ x => println(x); x }

  private
  val route = {
    val start = ByteString.empty
    val sep = ByteString("\n")
    val end = ByteString.empty

    implicit val ess: EntityStreamingSupport = EntityStreamingSupport.json()
      .withFramingRenderer(Flow[ByteString].intersperse(start, sep, end))

    get {
      path("ping") { complete("pong") } ~
      path("1") { complete( Data(1) ) } ~
      path("data") { complete(src) }
    }
  }

  implicit val as: ActorSystem = ActorSystem("TestRunSource")
  implicit val mat: Materializer = ActorMaterializer()
}

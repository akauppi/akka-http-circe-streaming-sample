package sandbox

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling._
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object TestRunSink extends FailFastCirceSupport with LazyLogging {

  // Due to:
  //  <<
  //    Error:(48, 54) could not find implicit value for parameter um: akka.http.scaladsl.unmarshalling.Unmarshaller[akka.util.ByteString,test.run.Data]
  //  <<
  //
  val evidence: Unmarshaller[ByteString,String] = implicitly[Unmarshaller[ByteString,String]]

  def main(args: Array[String]): Unit = {
    val port: Int = 8083    // (in real app, this would come from config)

    implicit val as: ActorSystem = ActorSystem("TestRunSink")
    implicit val mat: Materializer = ActorMaterializer()
    import as.dispatcher    // ExecutionContext

    val futResp: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"http://localhost:$port/data"))

    futResp.onComplete{
      case Success(resp) =>   // should get a line-wise stream of JSON objects

        // Based on -> https://doc.akka.io/docs/akka-http/current/common/json-support.html#consuming-json-streaming-style-apis
        //
        // tbd. try also this
        //
        /*** disabled; did not quite cut it, missing implicit
        val src: Source[Data, _ /*Future[NotUsed]*/] = {
          val unmarshalled: Future[Source[Data,NotUsed]] = Unmarshal(resp).to[Source[Data,NotUsed]]
          Source.fromFutureSource(unmarshalled)
        }
        ***/

        val src: Source[Data, _ /*Future[NotUsed]*/] = {
          resp.entity.dataBytes
            .via(jsonStreamingSupport.framingDecoder)
            .mapAsync(1)(bytes => Unmarshal(bytes).to[Data])
        }

        // Let's try how throttling here affects the remote end (it pushes at 100/sec)
        //
        src.throttle(10, 1 second)
          .runForeach(println)

      case Failure(th) =>
        logger.error(s"Request failed: ${th.getMessage}")
    }
  }

  /*implicit*/ val jsonStreamingSupport: JsonEntityStreamingSupport = {
    val start = ByteString.empty
    val sep = ByteString("\n")
    val end = ByteString.empty

    EntityStreamingSupport.json()
      .withFramingRenderer(Flow[ByteString].intersperse(start, sep, end))
  }
}
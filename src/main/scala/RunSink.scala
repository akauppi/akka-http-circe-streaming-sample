package sandbox

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import io.circe.{Decoder, Json, jawn}
import io.circe.parser.{parse => parseJson}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

// Note: For option '[B]', akka-http-circe (1.20.1) is not required. Having it enabled so we see if it affects '[A]'.
//
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

object TestRunSink extends FailFastCirceSupport with LazyLogging {

  def main(args: Array[String]): Unit = {
    val port: Int = 8083    // (in real app, this would come from config)

    import as.dispatcher    // ExecutionContext

    val futResp: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"http://localhost:$port/data"))

    futResp.onComplete{
      case Success(resp) =>   // should get a line-wise stream of JSON objects

        // Based on -> https://doc.akka.io/docs/akka-http/current/common/json-support.html#consuming-json-streaming-style-apis
        //
        val src: Source[Data,_] = {

          if (false) {    // [A]: does not compile
            //val tmp: Future[Source[Data,NotUsed]] = Unmarshal(resp).to[Source[Data,NotUsed]]
            //Source.fromFutureSource(tmp)
            ???

          } else {    // [B]: same, more explicitly
            resp.entity.dataBytes
              .via(jsonStreamingSupport.framingDecoder)
              .mapAsync(1){ bytes: ByteString => Unmarshal(bytes).to[Data] }
          }
        }

        // Let's try how throttling here affects the remote end (it pushes at 100/sec)
        //
        src.throttle(10, 1 second)
          .runForeach(println)

      case Failure(th) =>
        logger.error(s"Request failed: ${th.getMessage}")
    }
  }

  // Note: 'akka-http-circe' only provides unmarshalling from an 'HttpEntity', not from a 'ByteString' (which is
  //    required by the streaming unmarshalling.
  //
  /*** works, first try (no taking model of akka-http-circe)
  implicit def unmarshaller[T: Decoder]: Unmarshaller[ByteString,T] = {

    Unmarshaller.strict { bs: ByteString =>
      val s: String = bs.utf8String

      // tbd. How are unmarshalling errors supposed to be done? Below error handling is just-for-now.
      //
      parseJson(s) match {
        case Right(json) => json.as[T] match {
          case Right(x) => x
          case Left(decodingFailure) =>
            throw new RuntimeException(s"Unable to decode: $s")
        }
        case Left(s) =>
          throw new RuntimeException(s"Not valid JSON: $s")
      }
    }
  }
  ***/

  /*
  * Better? Modeled with ideas from 'akka-http-circe'
  *
  * Note: This is enough for '[B]' option
  */
  implicit def tUnmarshaller[T: Decoder]: Unmarshaller[ByteString,T] = {
    def decode(json: Json) = Decoder[T].decodeJson(json).fold(throw _, identity)
    jsonUnmarshaller.map(decode)
  }

  implicit
  private val jsonUnmarshaller: FromByteStringUnmarshaller[Json] = {
    Unmarshaller.strict {
      case ByteString.empty => throw Unmarshaller.NoContentException
      case data             => jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
    }
  }

  /*
  * Needed for '[A]' option
  */
  //implicit def respUnmarshaller[T: Decoder]: FromResponseUnmarshaller[Source[T,NotUsed]] = {
  //  ???
  //}

  // Note: 'implicit' needed only if we use the 'Unmarshal(resp)' approach
  //
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = {
    val start = ByteString.empty
    val sep = ByteString("\n")
    val end = ByteString.empty

    EntityStreamingSupport.json()
      .withFramingRenderer(Flow[ByteString].intersperse(start, sep, end))
  }

  implicit val as: ActorSystem = ActorSystem("TestRunSink")
  implicit val mat: Materializer = ActorMaterializer()
}

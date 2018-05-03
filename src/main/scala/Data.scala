package sandbox

import io.circe._, io.circe.generic.semiauto._

/*
* Sample class that we JSONify using circe
*/
case class Data(x: Int)

object Data {
  implicit val decoder: Decoder[Data] = deriveDecoder
  implicit val encoder: Encoder[Data] = deriveEncoder
}

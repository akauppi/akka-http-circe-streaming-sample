package test

import io.circe.Json
import io.circe.syntax._
import org.scalatest.{EitherValues, FlatSpec, Matchers}
import sandbox.Data

class DataTest extends FlatSpec with Matchers with EitherValues {

  behavior of "Data"

  it should "be convertible to JSON and back" in {

    val x: Json = Data(10).asJson

    x.as[Data].right.value shouldBe Data(10)
  }
}

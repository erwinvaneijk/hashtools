package nl.oakhill.hashtools.io

import java.io.File

import nl.oakhill.hashtools.HashResult
import nl.oakhill.hashtools.Digest
import spray.json.{DefaultJsonProtocol, JsArray, JsString, JsValue, RootJsonFormat}


/**
  * Helper class to help a HashResult be serialized in Json.
  *
  */
object HashResultJsonProtocol extends DefaultJsonProtocol {
  implicit object HashResultJsonFormat extends RootJsonFormat[HashResult] {
    def write(h: HashResult) =
        JsArray(JsString(h.path.toString), JsString(h.digest.algorithm), JsString(h.digest.toString))

    def read(value: JsValue) = value match {
      case JsArray(Vector(JsString(path), JsString(algorithm), JsString(digest))) =>
        HashResult(new File(path).toPath, Digest(algorithm, digest))
      case _ =>
        throw new IllegalArgumentException("hashResult expected")
    }
  }
}

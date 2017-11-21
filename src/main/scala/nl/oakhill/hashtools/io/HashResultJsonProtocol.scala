/*
 * Copyright (c) 2017 Erwin van Eijk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

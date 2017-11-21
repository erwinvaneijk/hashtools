/*
 * Copyright (c) 2017, Erwin J. van Eijk.
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

import java.io.OutputStream
import java.nio.charset.StandardCharsets

import scala.concurrent.Future
import monix.execution.{Ack, Scheduler}
import monix.execution.Ack.Continue
import monix.reactive.observers.Subscriber
import spray.json._

import nl.oakhill.hashtools.io.HashResultJsonProtocol._
import nl.oakhill.hashtools.HashResult


class ResultWriter(writer: OutputStream)(implicit s: Scheduler) extends Subscriber[HashResult] {

  override implicit def scheduler: Scheduler = s

  override def onNext(elem: HashResult): Future[Ack] = {
    val encoded = elem.toJson
    writer.write(encoded.compactPrint.getBytes(StandardCharsets.UTF_8))
    writer.write(Array[Byte](',', '\r', '\n'))
    Continue
  }

  override def onError(ex: Throwable): Unit = {
    writer.close()
  }

  override def onComplete(): Unit = {
    writer.close()
  }
}

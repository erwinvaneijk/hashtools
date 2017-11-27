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

import cats.data.State
import scala.concurrent.Future
import monix.execution.{Ack, Scheduler}
import monix.execution.Ack.Continue
import monix.reactive.observers.Subscriber
import spray.json._

import nl.oakhill.hashtools.io.HashResultJsonProtocol._
import nl.oakhill.hashtools.HashResult


/**
 * This class saves the state of all the comparisons.
 */
case class ComparisonResult(external: Set[HashResult], internal: Set[HashResult], validated: Set[HashResult], duplicate: Set[HashResult])


class ResultChecker(fileList: Set[HashResult])(implicit s: Scheduler) extends Subscriber[HashResult] {


  override implicit def scheduler: Scheduler = s

  override def onNext(elem: HashResult): Future[Ack] = {
    Continue
  }

  override def onError(ex: Throwable): Unit = {
  }

  override def onComplete(): Unit = {
  }

  private def compareResult(hashResult: HashResult,
    resultState: State[ComparisonResult]): State[ComparisonResult] = {
  }
}

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

import cats.data.State
import scala.concurrent.Future
import monix.execution.{Ack, Scheduler}
import monix.execution.Ack.Continue
import monix.reactive.observers.Subscriber

import nl.oakhill.hashtools.HashResult


/**
  * This class saves the state of all the comparisons.
  *
  * external: hashresults observed in the live filesystem
  * internal: the set of hashresults that are remaining after validation
  * validated: the set of hashresults that have been validated to be same
  * duplicate: the set of hashresults that have been validated more than once
  */
case class ComparisonResult(external: Set[HashResult], internal: Set[HashResult], validated: Set[HashResult], duplicate: Set[HashResult])


class ResultChecker(fileList: Set[HashResult])(implicit s: Scheduler) extends Subscriber[HashResult] {
  private var state = ComparisonResult(Set.empty, fileList, Set.empty, Set.empty)

  override implicit def scheduler: Scheduler = s

  override def onNext(elem: HashResult): Future[Ack] = {
    state = compareNextItem(elem, state)
    Continue
  }

  override def onError(ex: Throwable): Unit = {
  }

  override def onComplete(): Unit = {
  }

  def comparisonResult: ComparisonResult = state

  private def compareNextItem(hashResult: HashResult, state: ComparisonResult) = {
      if (state.internal(hashResult) && state.validated(hashResult)) {
        ComparisonResult(state.external + hashResult, state.internal - hashResult, state.validated, state.duplicate + hashResult)
      }
      else if (state.internal(hashResult)) {
        ComparisonResult(state.external, state.internal - hashResult, state.validated + hashResult, state.duplicate)
      }
      else {
        ComparisonResult(state.external + hashResult, state.internal, state.validated, state.duplicate)
      }
  }
}

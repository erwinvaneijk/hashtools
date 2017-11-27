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

package nl.oakhill.hashtools

import java.io.{ByteArrayOutputStream, File, FileNotFoundException, OutputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.Path

import monix.execution.Scheduler.Implicits.{global => scheduler}
import nl.oakhill.hashtools.io.ResultChecker
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ResultCheckerSpec extends FlatSpec with Matchers with MockFactory {
  "The ResultChecker" should "fulfill the contract" in {
    val hashResult = HashResult(new File("Hello.txt").toPath, Digest("MD5", "KtGOyCwgr3tZJu2c6mru3Q=="))
    val fileList = List[HashResult](hashResult)
    val resultChecker = new ResultChecker(fileList)(scheduler)
    val future = resultChecker.onNext(hashResult)
    resultChecker.onComplete()

    if (future.isCompleted) {
      val comparisonResult = resultChecker.comparisonResult
      comparisonResult.remainingInList should be (empty)
      comparisonResult.remainingInSet should be (empty)
      comparsionResult.validCheck should have size(1)
    }
    else {
      fail("Test took too long.")
    }
  }

  it should "accept only continue" in {
    val hashResult = HashResult(new File("Hello.txt").toPath, Digest("MD5", "KtGOyCwgr3tZJu2c6mru3Q=="))
    val fileList = List[HashResult](hashResult)
    val resultChecker = new ResultChecker(fileList)(scheduler)
    resultChecker.onComplete()

    val comparisonResult = resultChecker.comparisonResult
    comparisonResult.remainingInList should have size 1
    comparisonResult.remainingInSet should be (empty)
    comparsionResult.validCheck should have size(1)
  }

  it should "accept only error" in {
    val outputStream = new ByteArrayOutputStream()
    val resultChecker = new ResultChecker(outputStream)(scheduler)
    resultChecker.onError(new FileNotFoundException())

    val comparisonResult = resultChecker.comparisonResult
    comparisonResult.remainingInList should have size 1
    comparisonResult.remainingInSet should be (empty)
    comparsionResult.validCheck should have size(1)
  }
}

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
import nl.oakhill.hashtools.io.ResultWriter
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ResultWriterSpec extends FlatSpec with Matchers with MockFactory {
  "The ResultWriter" should "fulfill the contract" in {
    val outputStream = new ByteArrayOutputStream()
    val hashResult = HashResult(new File("Hello.txt").toPath, Digest("MD5", "KtGOyCwgr3tZJu2c6mru3Q=="))
    val resultWriter = new ResultWriter(outputStream)(scheduler)
    val future = resultWriter.onNext(hashResult)
    resultWriter.onComplete()

    if (future.isCompleted) {
      val result = outputStream.toString("UTF-8")
      result shouldEqual ("[\"Hello.txt\",\"MD5\",\"2AD18EC82C20AF7B5926ED9CEA6AEEDD\"],\r\n")
    }
    else {
      fail("Test took too long.")
    }
  }

  it should "accept only continue" in {
    val outputStream = new ByteArrayOutputStream()
    val resultWriter = new ResultWriter(outputStream)(scheduler)
    resultWriter.onComplete()

    val output = outputStream.toString("UTF-8")
    output should be (empty)
  }

  it should "accept only error" in {
    val outputStream = new ByteArrayOutputStream()
    val resultWriter = new ResultWriter(outputStream)(scheduler)
    resultWriter.onError(new FileNotFoundException())

    val output = outputStream.toString("UTF-8")
    output should be (empty)
  }
}

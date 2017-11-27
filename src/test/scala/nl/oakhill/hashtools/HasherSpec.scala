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

import java.io.{File, FileNotFoundException}

import monix.execution.Ack.Continue
import monix.execution.Scheduler.Implicits.{global => scheduler}
import nl.oakhill.hashtools.io.ResultWriter
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class HasherSpec extends FlatSpec with Matchers with MockFactory {
  val resultWriter = mock[ResultWriter]
  (resultWriter.onComplete _).expects().returning(Unit).repeat(0.to(2))

  "Hasher" should "have a working hashFile" in {
    (resultWriter.onNext _).expects(*).returning(Continue).repeat(3)
    val hasher = new Hasher(None, false, resultWriter)
    val resultList = hasher.hashFile(new File("src/main/resources/logback.xml"))
    resultList should not be (null)
    resultList should have size (3)
    val result = resultList.filter(_.digest.algorithm == "MD5").head
    result.path should be (new File("src/main/resources/logback.xml").toPath)
    result.digest.algorithm should be ("MD5")
    result.digest.toBase64 should be ("RoOvH03A1cRLSJORkm5CZA==")
  }

  it should "have a hashFile that handles non-existing files" in {
    val hasher = new Hasher(None, false, resultWriter)
    intercept[FileNotFoundException] {
      hasher.hashFile(new File("src/main/resources/no-such-file.xml"))
    }
  }

  it should "have a working hashDirectory" in {
    (resultWriter.onNext _).expects(*).returning(Continue).repeat(3)
    (resultWriter.onComplete _).expects().returning(Unit)
    val hasher = new Hasher(None, false, resultWriter)
    val result = hasher.hashDirectory(new File("src/main/resources").toPath)
    result should not be (null)
    result should have size(3)
    val resultFile = result.head
    resultFile.path should be (new File("src/main/resources/logback.xml").toPath)
    resultFile.digest.algorithm should be ("MD5")
    resultFile.digest.toBase64 should be ("RoOvH03A1cRLSJORkm5CZA==")
  }
}

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

import java.io.{File, FileInputStream}
import java.nio.file.Path
import java.security.MessageDigest

import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import monix.execution.Ack.{Continue, Stop}
import monix.execution.Scheduler
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import nl.oakhill.hashtools.io.RichFile.toRichFile
import resource.managed

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


case class HashResult(path: Path, digest: Digest)


class Hasher(output: Option[Path], verbose: Boolean, resultWriter: Subscriber[HashResult])(implicit s: Scheduler)
  extends LazyLogging {

  private val numberOfParallelComputations = 10

  private val BLOCK_READ_SIZE: Int = 1024 * 1024

  def hashDirectory(path: Path): List[HashResult] = hashDirectory(path, Duration.Inf)

  def hashDirectory(path: Path, timeOut: Duration): List[HashResult] = {
    logger.info(s"Looking at $path")
    val files = path.toFile.andTree(_.isFile)
    logger.info(s"${files.size} entries")
    val source = Observable.fromIterable[File](files)
    val processed = source.mapAsync(parallelism = numberOfParallelComputations) { file =>
      Task(hashFile(file))
    }
    val t = processed.toListL.runAsync
    val l = Await.result(t, timeOut).flatten
    resultWriter.onComplete()
    l
  }

  def hashFile(file: File): List[HashResult] = {
    logger.info(s"Processing ${file.toString}")
    managed(new FileInputStream(file)).map {
      input =>
        val algorithms = Map("MD5" -> MessageDigest.getInstance("MD5"),
          "SHA1" -> MessageDigest.getInstance("SHA1"),
          "SHA-256" -> MessageDigest.getInstance("SHA-256"))
        val buffer = new Array[Byte](BLOCK_READ_SIZE)

        def conclude(): List[HashResult] = {
          algorithms.map {
            case (name: String, algorithm: MessageDigest) =>
              val digest = Digest(name, algorithm.digest())
              val hashResult = HashResult(file.toPath, digest)
              resultWriter.onNext(hashResult).map {
                case Continue =>
                  Continue
                case Stop =>
                  Stop
              }
              hashResult
          }.toList
        }

        def read(): List[HashResult] = input.read(buffer) match {
          case -1 =>
            conclude()
          case n: Int =>
            algorithms.foreach { case (_: String, algorithm: MessageDigest) =>
              algorithm.update(buffer, 0, n)
            }
            read()
        }

        read()
    }.tried match {
      case f: Failure[List[HashResult]] =>
        throw f.exception
      case s: Success[List[HashResult]] =>
        s.value
    }
  }
}

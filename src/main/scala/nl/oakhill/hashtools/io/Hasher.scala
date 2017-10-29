package nl.oakhill.hashtools.io

// On evaluation a Scheduler is needed
import java.io.{File, FileInputStream}
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Base64

import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import resource._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class HashResult(path: Path, algorithm: String, digest: String)

class Hasher(input: Option[Path], output: Option[Path], verbose: Boolean) extends LazyLogging {

  val BLOCK_READ_SIZE: Int = 1024 * 1024

  def hashDirectory(path: Path): List[HashResult] = hashDirectory(path, Duration.Inf)

  def hashDirectory(path: Path, timeOut: Duration): List[HashResult] = {
    import RichFile._
    logger.info(s"Looking at $path")
    val files = path.toFile.andTree(_.isFile)
    logger.info(s"${files.size} entries")
    val source = Observable.fromIterable[File](files)
    val processed = source.mapAsync(parallelism = 10) { file =>
      Task(hashFile(file))
    }
    val t = processed.toListL.runAsync
    Await.result(t, timeOut).flatten
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
              val digest = Base64.getEncoder.encodeToString(algorithm.digest())
              HashResult(file.toPath, name, digest)
          }.toList
        }

        def read(): List[HashResult] = input.read(buffer) match {
          case -1 =>
            conclude()
          case n =>
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

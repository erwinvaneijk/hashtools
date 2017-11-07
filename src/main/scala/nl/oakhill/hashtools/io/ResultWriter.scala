package nl.oakhill.hashtools.io

import java.io.{FileOutputStream, OutputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.Path

import cats.syntax.writer
import monix.execution.{Ack, Scheduler}
import monix.execution.Ack.Continue
import monix.reactive.Observer
import monix.reactive.observers.Subscriber
import nl.oakhill.hashtools.HashResult
import spray.json._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future


class ResultWriter(writer: OutputStream)(implicit s: Scheduler) extends Subscriber[HashResult] {

  override implicit def scheduler: Scheduler = s

  override def onNext(elem: HashResult): Future[Ack] = {
    import nl.oakhill.hashtools.io.HashResultJsonProtocol._
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

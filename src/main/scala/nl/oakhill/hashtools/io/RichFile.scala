package nl.oakhill.hashtools.io

import java.io.File

import scala.language.implicitConversions

/**
  * A wrapper around file, allowing iteration either on direct children
  * or on directory tree
  **/
class RichFile(file: File) {

  def andTree: Iterable[File] = andTree((_: File)=>true)

  def andTree(predicate: (File=>Boolean)): Iterable[File] = streamTree(predicate)

  def streamTree : Stream[File] = streamTree((_:File) => true)

  def streamTree(predicate: (File=>Boolean)) : Stream[File] = {
    if (file.isDirectory) {
      Stream.cons(file, file.listFiles().toStream.flatMap { f: File => new RichFile(f).streamTree(predicate) }).filter(predicate)
    } else {
      Stream(file)
    }
  }

}

/**
  * implicitly enrich java.io.File with methods of RichFile
  * */
object RichFile {
  implicit def toRichFile(file: File): RichFile = new RichFile(file)
}
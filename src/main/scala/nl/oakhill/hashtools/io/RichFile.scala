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

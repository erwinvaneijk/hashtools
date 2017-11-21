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

import java.util.Base64

private[hashtools] trait DigestIdentifiers {
  val MD5_IDENTIFIER = "MD5"
  val SHA1_IDENTIFIER = "SHA-1"
  val SHA256_IDENTIFIER = "SHA-256"
}

class Digest(val algorithm: String, private val digest: Array[Byte]) extends DigestIdentifiers  {
  private val hexArray = "0123456789ABCDEF".toCharArray

  override def toString : String = {

    if (algorithm == MD5_IDENTIFIER) {
      toHex
    } else {
      toBase64
    }
  }

  def toBase64 : String = {
    Base64.getEncoder.encodeToString(digest)
  }

  def toHex : String = {
    digest.foldLeft(List[Char]()){
      case (s, b) =>
        val v: Integer = b & 0xFF
        hexArray(v & 0x0f) :: hexArray(v >> 4) :: s
    }.reverse.mkString
  }

  override def equals(t: Any): Boolean = {
    t match {
      case x: Digest =>
        (x.algorithm == algorithm) && (x.digest.sameElements(digest))
      case _ =>
        false
    }
  }

  override def hashCode(): Int = {
    algorithm.hashCode + digest.hashCode
  }
}


object Digest extends DigestIdentifiers {
  def apply(algorithm: String, digest: Array[Byte]) : Digest = new Digest(algorithm, digest)

  def apply(algorithm: String, string: String) : Digest = {
    new Digest(algorithm,
      if (algorithm == MD5_IDENTIFIER) {
        convertMd5(string)
      }
      else if (algorithm == SHA1_IDENTIFIER){
        convertSha1(string)
      } else if  (algorithm == SHA256_IDENTIFIER) {
        convertSha256(string)
      } else {
        throw new IllegalArgumentException(s"algorithm ${algorithm} not supported")
      }
    )
  }

  private def convertMd5(string: String) = convertString(MD5_IDENTIFIER, string, 32, 24)

  private def convertSha1(string: String) = convertString(SHA1_IDENTIFIER, string, 40, 28)

  private def convertSha256(string: String) = convertString(SHA256_IDENTIFIER, string, 64, 44)

  private def convertString(algorithm: String, string: String, hexSize: Int, base64Size: Int): Array[Byte] = {
    string.length match {
      case `base64Size` =>
        Base64.getDecoder.decode(string)
      case `hexSize` =>
        javax.xml.bind.DatatypeConverter.parseHexBinary(string)
      case x: Int =>
      throw new IllegalArgumentException(s"invalid $algorithm String digest length: $x")
    }
  }
}

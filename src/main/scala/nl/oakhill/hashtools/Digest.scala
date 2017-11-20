package nl.oakhill.hashtools

import java.util.Base64

class Digest(val algorithm: String, private val digest: Array[Byte]) {
  private val hexArray = "0123456789ABCDEF".toCharArray

  override def toString : String = {
    if (algorithm == "MD5") {
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


object Digest {
  def apply(algorithm: String, digest: Array[Byte]) : Digest = new Digest(algorithm, digest)

  def apply(algorithm: String, string: String) : Digest = {
    new Digest(algorithm,
      if (algorithm == "MD5") {
        convertMd5(string)
      }
      else if (algorithm == "SHA-1"){
        convertSha1(string)
      } else if  (algorithm == "SHA-256") {
        convertSha256(string)
      } else {
        throw new IllegalArgumentException(s"algorithm ${algorithm} not supported")
      }
    )
  }

  private def convertMd5(string: String) = convertString("MD5", string, 32, 24)
  private def convertSha1(string: String) = convertString("SHA-1", string, 40, 28)
  private def convertSha256(string: String) = convertString("SHA-256", string, 64, 44)

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

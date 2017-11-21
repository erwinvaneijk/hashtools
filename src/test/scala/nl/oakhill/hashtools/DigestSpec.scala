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

class DigestSpec extends FlatSpec with Matchers with MockFactory {
  val zeroHexDigest = "00000000000000000000000000000000"
  val zeroBaseDigest = "AAAAAAAAAAAAAAAAAAAAAA=="

  "Digest" should "have working constructor for hex" in {
    val d = Digest("MD5", zeroHexDigest)
    d.toString should be (zeroHexDigest)
  }

  it should "have working constructor for base64" in {
    val d = Digest("MD5", zeroBaseDigest)
    d.toString should be (zeroHexDigest)
    d.toHex should be (zeroHexDigest)
    d.toBase64 should be (zeroBaseDigest)
  }

  it should "behave properly on zero bytes" in {
    val array = new Array[Byte](20)
    val d = Digest("SHA-1", array)
    d.algorithm should be ("SHA-1")
    d.toString should be ("AAAAAAAAAAAAAAAAAAAAAAAAAAA=")

    val digest1 = Digest(d.algorithm, d.toHex)
    val digest2 = Digest(d.algorithm, d.toBase64)
    digest1 should be (digest2)
    digest2 should be (digest1)
  }

  it should "convert seamlessly from base64 to hex" in {
    val d = Digest("MD5", zeroBaseDigest)
    d.toString should be (zeroHexDigest)
    d.toBase64 should be (zeroBaseDigest)
  }

  it should "convert from base64 to hex for sha256" in {
    val sha256digest = "kin1eycEEKnx92KDCcjqHHrqph7LXMH44fry0jEe+Mc="
    val d = Digest("SHA-256", sha256digest)
    d.toBase64 should be (sha256digest)
    d.toString should be (sha256digest)
    d.toHex should be ("9229F57B270410A9F1F7628309C8EA1C7AEAA61ECB5CC1F8E1FAF2D2311EF8C7")

    val d2 = Digest(d.algorithm, d.toHex)
    d should be (d2)
  }

  it should "throw an exception when an illegal sized string is given" in {
    val brokenZeroHexDigest = "0000000000000000000000000000000000"
    assertThrows[IllegalArgumentException] {
      Digest("MD5", brokenZeroHexDigest)
    }
    assertThrows[IllegalArgumentException] {
      Digest("SHA-1", brokenZeroHexDigest)
    }
    assertThrows[IllegalArgumentException] {
      Digest("SHA-256", brokenZeroHexDigest)
    }
  }
}

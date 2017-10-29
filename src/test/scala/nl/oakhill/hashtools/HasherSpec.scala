package nl.oakhill.hashtools

import java.io.{File, FileNotFoundException}

import nl.oakhill.hashtools.io.Hasher
import org.scalatest.{FlatSpec, Matchers}

class HasherSpec extends FlatSpec with Matchers {
  "Hasher" should "have a working hashFile" in {
    val hasher = new Hasher(None, None, false)
    val resultList = hasher.hashFile(new File("src/main/resources/logback.xml"))
    resultList should not be (null)
    resultList should have size (3)
    val result = resultList.filter(_.algorithm == "MD5").head
    result.path should be (new File("src/main/resources/logback.xml").toPath)
    result.algorithm should be ("MD5")
    result.digest should be ("fbLsI60dVB2FwOOlQdmtpQ==")
  }

  it should "have a hashFile that handles non-existing files" in {
    val hasher = new Hasher(None, None, false)
    intercept[FileNotFoundException] {
      hasher.hashFile(new File("src/main/resources/no-such-file.xml"))
    }
  }

  it should "have a working hashDirectory" in {
    val hasher = new Hasher(None, None, false)
    val result = hasher.hashDirectory(new File("src/main/resources").toPath)
    result should not be (null)
    result should have size(3)
    val resultFile = result.head
    resultFile.path should be (new File("src/main/resources/logback.xml").toPath)
    resultFile.algorithm should be ("MD5")
    resultFile.digest should be ("fbLsI60dVB2FwOOlQdmtpQ==")
  }
}

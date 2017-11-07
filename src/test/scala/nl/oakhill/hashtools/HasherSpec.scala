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
    val hasher = new Hasher(None, None, false, resultWriter)
    val resultList = hasher.hashFile(new File("src/main/resources/logback.xml"))
    resultList should not be (null)
    resultList should have size (3)
    val result = resultList.filter(_.algorithm == "MD5").head
    result.path should be (new File("src/main/resources/logback.xml").toPath)
    result.algorithm should be ("MD5")
    result.digest should be ("fbLsI60dVB2FwOOlQdmtpQ==")
  }

  it should "have a hashFile that handles non-existing files" in {
    val hasher = new Hasher(None, None, false, resultWriter)
    intercept[FileNotFoundException] {
      hasher.hashFile(new File("src/main/resources/no-such-file.xml"))
    }
  }

  it should "have a working hashDirectory" in {
    (resultWriter.onNext _).expects(*).returning(Continue).repeat(3)
    (resultWriter.onComplete _).expects().returning(Unit)
    val hasher = new Hasher(None, None, false, resultWriter)
    val result = hasher.hashDirectory(new File("src/main/resources").toPath)
    result should not be (null)
    result should have size(3)
    val resultFile = result.head
    resultFile.path should be (new File("src/main/resources/logback.xml").toPath)
    resultFile.algorithm should be ("MD5")
    resultFile.digest should be ("fbLsI60dVB2FwOOlQdmtpQ==")
  }
}

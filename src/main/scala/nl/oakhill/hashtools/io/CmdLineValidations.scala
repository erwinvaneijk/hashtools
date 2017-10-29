package nl.oakhill.hashtools.io

import java.nio.file.Path

object CmdLineValidations {
  /**
    * Validate that path exists in the filesystem.
    *
    * @param path the path to validate.
    * @return true when the path exists
    */
  def pathShouldExist(path: Option[Path]): Boolean = {
    path match {
      case Some(p: Path) =>
        p.toFile.exists()
      case _ =>
        true
    }
  }

  /**
    * Validate that path does not yet exist in the filesystem.
    *
    * @param path the path to validate.
    * @return when the path does not exist.
    */
  def pathShouldNotExist(path: Option[Path]): Boolean = {
    path match {
      case Some(p: Path) =>
        !p.toFile.exists()
      case _ =>
        true
    }
  }
}

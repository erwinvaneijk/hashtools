package nl.oakhill.hashtools

import java.nio.file.Path

import cats.implicits._
import com.monovore.decline.{CommandApp, Opts}

object Main extends CommandApp(
  name = "hashtools",
  header = "Hash directories",
  main = {
    val inputOpt =
      Opts.option[Path]("input", short="i", metavar="Filename", help = "A file to read the list if hashes from.")
        .orNone
        .validate("The input file should exist")(CmdLineValidations.pathShouldExist)
    val outputOpt =
      Opts.option[Path]("output", short="o", metavar="Filename", help = "A file to write the resulting hashes to.")
        .orNone
          .validate("The output file should not exist")(CmdLineValidations.pathShouldNotExist)
    val verboseOption =
      Opts.flag("verbose", short="v", help = "Increase the verbosity of the processing.").orTrue
    val startDirectories =
      Opts.arguments[Path](metavar = "directory")

    (inputOpt, outputOpt, verboseOption, startDirectories).mapN( {
      (input, output, verbose, directories) =>
      val hasher = new Hasher(input, output, verbose)
      directories.map(hasher.hashDirectory)
    }
    )
  }
)


package nl.oakhill.hashtools

import java.io.{FileOutputStream, OutputStream}
import java.nio.file.Path

import cats.implicits._
import cats.syntax.writer
import com.monovore.decline.{CommandApp, Opts}
import monix.execution.Scheduler.Implicits.{global => scheduler}
import monix.reactive.OverflowStrategy
import monix.reactive.observers.{BufferedSubscriber, Subscriber}
import nl.oakhill.hashtools.io.ResultWriter

object Main extends CommandApp(
  name = "hashtools",
  header = "Hash directories",
  main = {
    val inputOpt =
      Opts.option[Path]("input", short = "i", metavar = "Filename", help = "A file to read the list if hashes from.")
        .orNone
        .validate("The input file should exist")(CmdLineValidations.pathShouldExist)
    val overwriteOption = Opts.flag("overwrite", short = "f", help = "Overwrite any outputs if they exist.").orFalse
    val outputOpt =
      Opts.option[Path]("output", short = "o", metavar = "Filename", help = "A file to write the resulting hashes to.")
        .orNone
    val verboseOption =
      Opts.flag("verbose", short = "v", help = "Increase the verbosity of the processing.").orTrue
    val startDirectories =
      Opts.arguments[Path](metavar = "directory")

    (inputOpt, outputOpt, verboseOption, overwriteOption, startDirectories).mapN({
      (input, output, verbose, overwrite, directories) =>
        val writer: OutputStream = if (output.isDefined && CmdLineValidations.pathShouldNotExist(output) || overwrite) {
          new FileOutputStream(output.get.toFile)
        } else if (output.isDefined && CmdLineValidations.pathShouldNotExist(output)) {
          new FileOutputStream(output.get.toFile)
        } else if (output.isDefined && CmdLineValidations.pathShouldNotExist(output) || !overwrite) {
          throw new IllegalArgumentException("File {} already exists. Aborting.".format(output.get.toFile().getAbsolutePath))
        } else {
          System.out
        }
        val resultWriter: Subscriber[HashResult] = new ResultWriter(writer)
        val bufferedWriter = BufferedSubscriber.synchronous[HashResult](resultWriter, OverflowStrategy.Unbounded)
        val hasher = new Hasher(input, output, verbose, bufferedWriter)(scheduler)
        directories.map(hasher.hashDirectory)
    }
    )
  }
)


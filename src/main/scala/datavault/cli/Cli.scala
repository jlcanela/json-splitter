package jsonsplitter.cli

import zio._
import java.nio.file.Paths

trait CreatePath {

  def input: String
  def output: String

  def inputPath = ZIO
    .fromOption(Option(Paths.get(input)))
    .mapError(_ => new IllegalArgumentException("invalid input"))
  def outputPath = ZIO
    .fromOption(Option(Paths.get(output)))
    .mapError(_ => new IllegalArgumentException("invalid output"))
}

sealed trait CommandParam
final case class ErrorCommandParam(message: String) extends CommandParam
final case class SplitJsonCommandParam(input: String, output: String)
    extends CommandParam
    with CreatePath

object Cli {
  def parse(args: Array[String]): CommandParam = args.toList match {
    case List("split-json", input, output) => SplitJsonCommandParam(input, output)
    case List()            => ErrorCommandParam("no parameter")
    case _                 => ErrorCommandParam("unknown parameter")
  }
}

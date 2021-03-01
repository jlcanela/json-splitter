package jsonsplitter.cli

import zio._
import jsonsplitter.filesystem.JPath

trait CreatePath {

  def input: String
  def output: String

  def inputPath = ZIO
    .fromEither(JPath(input)) //Option(Paths.get(input)))
    .mapError(_ => new IllegalArgumentException("invalid input"))
  def outputPath = ZIO
    .fromEither(JPath(output)) // Option(Paths.get(output)))
    .mapError(_ => new IllegalArgumentException("invalid output"))
}

sealed trait CommandParam
final case class ErrorCommandParam(message: String) extends CommandParam
final case class SplitJsonCommandParam(input: String, output: String)
    extends CommandParam
    with CreatePath
final object ShowBuckets extends CommandParam

object Cli {
  def parse(args: Array[String]): CommandParam = args.toList match {
    case List("split-json", input, output) => SplitJsonCommandParam(input, output)
    case List("show-buckets")              => ShowBuckets
    case List()                            => ErrorCommandParam("no parameter")
    case _                                 => ErrorCommandParam("unknown parameter")
  }
}

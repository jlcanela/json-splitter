package jsonsplitter

import zio._
import zio.s3._
import zio.console.Console
import zio.clock.Clock
import zio.blocking.Blocking
import cli.{Cli, ErrorCommandParam, SplitJsonCommandParam}
import command.Command
import splitter.Splitter
import jsonsplitter.cli.ShowBuckets

import software.amazon.awssdk.regions.Region
object SplitJsonCli {

  val s3Client = S3Credentials("test", "test")

  def s3Service = zio.s3.Live
    .connect(Region.EU_CENTRAL_1, s3Client, Some(new java.net.URI("http://localhost:4566")))

  def s3Layer: Layer[Throwable, Has[S3.Service]] = ZLayer.fromManaged(s3Service)

  def deps =
    Console.live >+> Blocking.live >+> Clock.live >+> s3Layer >+> Splitter.live >+> Command.live

  def cmd(
      args: Array[String]
  ): ZIO[Command with Splitter with Console with S3 with Clock with Blocking, Throwable, Int] =
    Cli.parse(args) match {
      case ErrorCommandParam(message) => Command.showMessage(message)
      case sjc: SplitJsonCommandParam => Command.splitJson(sjc)
      case ShowBuckets                => Command.showBuckets
      case _                          => Command.showMessage("unknown command")
    }

  def run(args: Array[String]) = {
    val runtime = zio.Runtime.default
    val result  = runtime.unsafeRun(cmd(args).provideLayer(deps))
    if (result > Command.NO_COMMAND) System.exit(result)
  }

  def main(args: Array[String]) = run(args)

}

package jsonsplitter

import zio._
import zio.console.Console
import cli.{Cli, ErrorCommandParam, SplitJsonCommandParam}
import command.Command
import splitter.Splitter

object SplitJsonCli {

  def deps = Console.live >+> Splitter.live >+> Command.live

  def cmd(args: Array[String]): ZIO[Command with Splitter with Console, Throwable, Int] =
    Cli.parse(args) match {
      case ErrorCommandParam(message) => Command.showMessage(message)
      case sjc: SplitJsonCommandParam => Command.splitJson(sjc)
      case _                          => Command.showMessage("unknown command")
    }

  def run(args: Array[String]) = {
    val runtime = zio.Runtime.default
    val result  = runtime.unsafeRun(cmd(args).provideLayer(deps))
    if (result > Command.NO_COMMAND) System.exit(result)
  }

  def main(args: Array[String]) = run(args)

}

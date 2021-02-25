package jsonsplitter.command

import zio._
import zio.console._

import jsonsplitter.cli.{SplitJsonCommandParam}
import jsonsplitter.splitter.Splitter

object Command {

  val SUCCESS    = 0
  val NO_COMMAND = 1

  // Service definition
  trait Service {
    def showMessage(message: String): ZIO[Console, Nothing, Int]
    def splitJson(sjc: SplitJsonCommandParam): ZIO[Splitter with Console, Throwable, Int]
  }

  // Module implementation
  val live: ZLayer[Splitter with Console, Nothing, Command] = ZLayer.succeed {
    new Service {
      def showMessage(message: String): ZIO[Console, Nothing, Int] = for {
        _ <- putStrLn(message)
      } yield NO_COMMAND

      def splitJson(sjc: SplitJsonCommandParam): ZIO[Splitter with Console, Throwable, Int] = for {
        input  <- sjc.inputPath
        output <- sjc.outputPath
        _      <- putStrLn(s"splitjson file '$input' to folder '$output'")
        _      <- Splitter.splitJson(input, output)
      } yield SUCCESS

    }
  }

  def showMessage(message: String): ZIO[Command with Console, Nothing, Int] =
    ZIO.accessM(_.get.showMessage(message))

  def splitJson(
      sjc: SplitJsonCommandParam
  ): ZIO[Command with Splitter with Console, Throwable, Int] =
    ZIO.accessM(_.get.splitJson(sjc))

}

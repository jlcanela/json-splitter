package jsonsplitter.acceptance

import zio._
import zio.test.Assertion._
import zio.test._

import java.time.Duration

import jsonsplitter.SplitJsonCli

object CommandSpec extends DefaultRunnableSpec {
  val deps = SplitJsonCli.deps

  val SUCCESS                                            = 0
  val FAILURE                                            = 1
  val params                                             = Array("split-json", "src/test/resources/sample.json", "out-test")
  def run(args: Array[String]): ZIO[Any, Throwable, Int] = SplitJsonCli.cmd(args).provideLayer(deps)

  /* def runAndRead = for {
    _       <- run(params)
    //_  <- ZIO.effect(Thread.sleep(500))
    //_ <- ZIO.succeed(()).delay(Duration.ofMillis(500))
    //content <- IO(scala.io.Source.fromFile("out-test/a.json").getLines.mkString)
   // _       <- console.putStrLn(content)
  } yield content
   */
  def spec = suite("Command")(
    testM("split json must succeed") {
      assertM(run(params))(equalTo(SUCCESS))
    } /*,
    testM("split json must create a file") {
      assertM(runAndRead)(equalTo("""{ "nodeType": "a"}"""))
    }*/
  )
}

package jsonsplitter.splitter

import zio._
import zio.stream._
import zio.console.Console

//import java.util.zip._
//import java.io.InputStream
import java.io.Writer
import java.nio.file.{Files, Path, Paths}
//import java.nio.file.StandardCopyOption.REPLACE_EXISTING
/*
import com.fasterxml.jackson.dataformat.csv._
import com.fasterxml.jackson.databind.{MappingIterator, ObjectWriter}
 */

object Splitter {

//  final case class Table(name: String, headers: Array[String], rows: Stream[Any, Array[String]])

//  final case class File(inputStream: InputStream, name: String, path: String)

  // Service definition
  trait Service {

    def splitJson(path: Path, destination: Path): ZIO[Console, Throwable, Unit]

  }

  // Module implementation
  val live: ZLayer[Console, Nothing, Splitter] = ZLayer.succeed {
    new Service {

      def createStream(path: Path): ZIO[Any, Throwable, ZStream[Any, Throwable, String]] = {
        for {
          is <- IO(Files.newInputStream(path))
          f     = scala.io.Source.fromInputStream(is)
          lines = f.getLines
        } yield Stream.fromIterator[String](lines)
      }

      val NodeType = """.*\"nodeType"\s*\:\s*\"([a-zA-Z0-9_-]+)\".*""".r
      def splitJson(path: Path, destination: Path): ZIO[Console, Throwable, Unit] = {

        def showString(s: String) = s match {
          case NodeType(nodeType) => console.putStrLn(nodeType)
          case s                  => console.putStrLn(s"$s not recognized")
        }

        val Extr = """.*\"nodeType\"\s*\:\s*"([a-zA-Z0-9_-]+)\".*""".r
        def createWriter(state: Map[String, Writer], name: String) = for {
          _  <- IO(Files.createDirectories(Paths.get("out")))
          os <- IO(Files.newOutputStream(Paths.get("out", s"${name}.json")))
          b  <- IO(new java.io.BufferedWriter(new java.io.OutputStreamWriter(os)))
        } yield (state + (name -> b), b)

        def writeLine(
            state: Map[String, Writer],
            line: String
        ): ZIO[Any, Throwable, Map[String, Writer]] = {
          val name = Extr.unapplySeq(line).map(_.head).getOrElse("default")
          for {
            (state, writer) <- IO
              .fromOption(state.get(name).map((state, _)))
              .orElse(createWriter(state, name))
            _ <- IO(writer.write(line + "\n"))
          } yield state
        }

        for {
          stream1 <- createStream(path)
          state   <- stream1.foldM(Map[String, Writer]())(writeLine _)
          _ = state.values.foreach(_.close())
        } yield ()
      }
    }
  }

  def splitJson(path: Path, destination: Path): ZIO[Splitter with Console, Throwable, Unit] =
    ZIO.accessM(_.get.splitJson(path, destination))
}

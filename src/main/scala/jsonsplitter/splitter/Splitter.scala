package jsonsplitter.splitter

import zio._
import zio.stream._
import zio.console.Console
import zio.clock.Clock
import zio.blocking.Blocking
import java.time.Duration

import zio.s3._

import java.nio.file.{Files, Path, Paths}
import jsonsplitter.filesystem._

object Splitter {

  // Service definition
  trait Service {

    def splitJson(
        path: JPath,
        destination: JPath
    ): ZIO[Clock with S3 with Console with Blocking, Throwable, Unit]
    def showBuckets: ZIO[Clock with S3 with Blocking with Console, Throwable, Unit]

  }

  // Module implementation
  val live: ZLayer[Console with S3, Nothing, Splitter] = ZLayer.succeed {
    new Service {

      def dataStream = for {
        queue <- Queue.unbounded[Byte]
        feedQueue = queue.offerAll("a sample data".getBytes.toIterable)
        stream    = ZStream.fromQueueWithShutdown(queue)
      } yield (feedQueue, stream)

      def writeS3Data(
          zs: ZStream[Blocking with S3 with Console, Throwable, Byte],
          bucket: String,
          path: String
      ) = for {
        _ <- console.putStrLn(s"write to s3://$bucket/$path")
        _ <- multipartUpload(bucket, path, zs)(4)
      } yield ()

      def writeFileData(zs: ZStream[Any, Throwable, Byte], path: Path) = for {
        _ <- ZIO
          .fromOption(Option(path.getParent))
          .map(path => Files.createDirectories(path)) orElse ZIO.succeed(())
        _ <- zs.run(ZSink.fromFile(path))
      } yield ()

      def createQueue(destination: JPath, name: String) = for {
        queue <- Queue.unbounded[String]
        zs = ZStream.fromQueueWithShutdown(queue).mapConcatChunk(s => Chunk.fromArray(s.getBytes()))
        _ <- destination match {
          case LocalFile(path: Path) => writeFileData(zs, path.resolve(name)).fork
          case S3File(bucket: String, path: String) =>
            writeS3Data(zs, bucket, s"${path}/${name}").fork
        }
      } yield queue

      def showBuckets: ZIO[Blocking with Console with S3 with Clock, Throwable, Unit] = for {
        _ <- console.putStrLn("showing buckets")
        st = createStream(S3File("mybucket", "in/sample.json"))
        queue <- createQueue(S3File("mybucket", "out"), "default")
        _     <- st.run(Sink.foreach((s: String) => queue.offer(s))).ensuring(queue.shutdown)
        _     <- ZIO.succeed(()).delay(Duration.ofMillis(1500))
      } yield ()

      def createS3Stream(path: S3File): ZStream[S3, Throwable, Byte] =
        s3.getObject(path.bucket, path.path)

      def createLocalStream(path: Path): ZStream[Blocking, Throwable, Byte] =
        ZStream.fromFile(path)

      def gunzip(stream: ZStream[S3 with Blocking, Throwable, Byte], path: String) = if (
        path.endsWith(".gz")
      ) {
        stream.transduce(ZTransducer.gunzip(1024 * 64))
      } else {
        stream
      }

      def createStream(path: JPath): ZStream[S3 with Blocking, Throwable, String] = {

        def stream = path match {
          case path @ S3File(_, key) => gunzip(createS3Stream(path), key)
          case LocalFile(path)       => gunzip(createLocalStream(path), path.toString)
        }

        stream
          .transduce(ZTransducer.utf8Decode)
          .transduce(ZTransducer.splitLines)
      }

      val ExtractName = """.*\"nodeType\"\s*\:\s*"([a-zA-Z0-9_-]+)\".*""".r
      def findName(line: String) = ExtractName
        .unapplySeq(line)
        .map(_.head)
        .getOrElse("default")

      def addQueue(destination: JPath)(
          state: Map[String, Queue[String]],
          name: String
      ): ZIO[
        Blocking with S3 with Console,
        Throwable,
        (Map[String, Queue[String]], Queue[String])
      ] = for {
        queue <- createQueue(destination, name)
      } yield (state + (name -> queue), queue)

      def write(destination: JPath)(
          state: Map[String, Queue[String]],
          line: String
      ): ZIO[Blocking with S3 with Console, Throwable, Map[String, Queue[String]]] = for {
        name <- ZIO.succeed(findName(line))
        (updatedState, queue) <- ZIO
          .fromOption(state.get(name).map(state -> _))
          .orElse(addQueue(destination)(state, name))
        _ <- queue.offer(line)
      } yield updatedState

      def splitJson(
          path: JPath,
          destination: JPath
      ): ZIO[Console with S3 with Blocking with Clock, Throwable, Unit] = {

        for {
          stream <- IO.succeed(createStream(path))
          init = Map[String, Queue[String]]()
          finalState <- stream.foldM(init)(write(destination))
          _          <- ZIO.foreachPar(finalState.values)(_.shutdown)
          _          <- ZIO.succeed(()).delay(Duration.ofMillis(3000))
        } yield ()
      }
    }
  }

  def splitJson(
      path: JPath,
      destination: JPath
  ): ZIO[Splitter with S3 with Clock with Console with Blocking, Throwable, Unit] =
    ZIO.accessM(_.get.splitJson(path, destination))

  def showBuckets: ZIO[Splitter with Clock with S3 with Blocking with Console, Throwable, Unit] =
    ZIO.accessM(_.get.showBuckets)

}

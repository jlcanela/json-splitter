package jsonsplitter.filesystem

import java.nio.file.{Path, Paths}

sealed trait JPath
case class LocalFile(path: Path)                extends JPath
case class S3File(bucket: String, path: String) extends JPath

object JPath {
  val S3Reg = "[s|S]3://([a-zA-Z0-9]+)/(.*)".r

  def apply(path: String): Either[Option[Nothing], JPath] = path match {
    case S3Reg(bucket, path) => Right(S3File(bucket, path))
    case path                => Option(Paths.get(path)).map(LocalFile).toRight(None)
  }
}

package finder.domain

import java.nio.file.Paths

import cats.effect._
import fs2.{ io, text, Pipe, Stream }

trait FileReaderWriter[F[_], Ctx] {
  def inputStream(path: String): Stream[F, Ctx]
  def toFile(path: String, blocker: Blocker): Pipe[F, Ctx, Unit]
}

final class StringFileReaderWriter[F[_]: ContextShift: Sync] extends FileReaderWriter[F, String] {
  override def inputStream(path: String): Stream[F, String] =
    Stream.resource(Blocker[F]).flatMap { blocker =>
      io.file
        .readAll(Paths.get(path), blocker, 4096)
        .through(text.utf8Decode[F])
        .through(text.lines[F])
    }

  override def toFile(path: String, blocker: Blocker): Pipe[F, String, Unit] =
    _.intersperse("\n")
      .through(text.utf8Encode)
      .through(io.file.writeAll(Paths.get(path), blocker))
}

object FileReaderWriter {
  def makeStringReader[F[_]: ContextShift: Sync]: F[StringFileReaderWriter[F]] = Sync[F].delay {
    new StringFileReaderWriter[F]
  }

}

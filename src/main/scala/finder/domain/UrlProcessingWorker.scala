package finder.domain

import cats.effect._
import cats.implicits._
import finder.model.{ Keyword, KeywordCount, Url }
import fs2.Stream

trait UrlProcessingWorker[F[_]] {
  def start(urlsPath: String, resultPath: String, keywords: List[Keyword])(
      handler: Url => F[List[KeywordCount]]
  ): F[Unit]
}

final class DefaultUrlProcessingWorker[F[_]: ContextShift: Sync: Concurrent](
    fileReaderWriter: StringFileReaderWriter[F],
    maxWorkers: Int
) extends UrlProcessingWorker[F] {
  // Semaphore is used to limit number of created threads for urls to not create thousands of fibers
  override def start(urlsPath: String, resultPath: String, keywords: List[Keyword])(
      handler: Url => F[List[KeywordCount]]
  ): F[Unit] =
    Stream
      .resource(Blocker[F])
      .flatMap { blocker =>
        fileReaderWriter
          .inputStream(urlsPath)
          .parEvalMapUnordered(maxWorkers) { urlStr =>
            handler(Url(urlStr))
              .map(_.foldLeft(urlStr) {
                case (acc, count) => acc + "," + count.count.toString
              })
          }
          .cons1(keywords.foldLeft("URL") {
            case (acc, keyword) => acc + "," + keyword.value
          }.concat("\n"))
          .through(fileReaderWriter.toFile(resultPath, blocker))
      }
      .compile
      .drain
}

object UrlProcessingWorker{
  def make[F[_]: ContextShift: Concurrent: Sync](fileReaderWriter: StringFileReaderWriter[F], maxWorkers: Int): F[UrlProcessingWorker[F]] =
    Sync[F].delay{
      new DefaultUrlProcessingWorker[F](fileReaderWriter, maxWorkers)
    }
}

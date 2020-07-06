package finder.domain

import cats.effect.{ ContextShift, Sync }
import finder.model.Keyword

trait KeywordLoader[F[_]] {
  def load(path: String): F[List[Keyword]]
}

final class DefaultKeywordLoader[F[_]: Sync: ContextShift](fileReader: StringFileReaderWriter[F]) extends KeywordLoader[F] {
  override def load(path: String): F[List[Keyword]] =
    fileReader.inputStream(path).map(Keyword(_)).compile.toList
}

object KeywordLoader {
  def makeDefaultKeywordLoader[F[_]: Sync: ContextShift](fileReader: StringFileReaderWriter[F]): F[KeywordLoader[F]] =
    Sync[F].delay(new DefaultKeywordLoader[F](fileReader))
}

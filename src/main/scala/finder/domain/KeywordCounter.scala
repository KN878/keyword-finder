package finder.domain

import cats.Applicative
import cats.implicits._
import finder.model.{ Keyword, KeywordCount }

trait KeywordCounter[F[_], Ctx] {
  def count(keyword: Keyword, context: Ctx): F[KeywordCount]
}

final class DefaultKeywordCounter[F[_]: Applicative] extends KeywordCounter[F, String] {
  override def count(keyword: Keyword, context: String): F[KeywordCount] =
    KeywordCount(keyword, context.split(keyword.value).length - 1).pure[F]
}

object KeywordCounter {
  def make[F[_]: Applicative]: DefaultKeywordCounter[F] =
    new DefaultKeywordCounter[F]
}

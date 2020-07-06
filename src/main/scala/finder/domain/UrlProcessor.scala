package finder.domain

import cats._
import cats.effect.Sync
import cats.implicits._
import finder.model.{ InvalidUri, Keyword, KeywordCount, Url }
import tofu._
import tofu.logging._
import tofu.syntax.handle._

trait UrlProcessor[F[_]] {
  def process(keywords: List[Keyword], url: Url): F[List[KeywordCount]]
}

final class DefaultUrlProcessor[F[_]: Parallel: Monad: Logging, Ctx <: String](
    client: HttpClient[F, Ctx],
    counter: KeywordCounter[F, Ctx]
)(
    implicit H1: Handle[F, InvalidUri]
) extends UrlProcessor[F] {
  override def process(keywords: List[Keyword], url: Url): F[List[KeywordCount]] =
    client
      .get(url)
      .flatMap {
        case Some(ctx) =>
          keywords.parTraverse(counter.count(_, ctx))
        case None => keywords.map(KeywordCount(_, 0)).pure[F]
      }
      .handleWith { e: InvalidUri =>
        Logging[F].error(e.value) *> keywords.map(KeywordCount(_, 0)).pure[F]
      }
}

object UrlProcessor {
  def make[I[_]: Functor, F[_]: Parallel: Monad: Sync](client: HttpClient[F, String], counter: KeywordCounter[F, String])(
      implicit H1: HandleTo[F, F, InvalidUri],
      logs: Logs[I, F]
  ): I[DefaultUrlProcessor[F, String]] =
    logs.forService[UrlProcessor[F]].map(implicit l => new DefaultUrlProcessor[F, String](client, counter))
}

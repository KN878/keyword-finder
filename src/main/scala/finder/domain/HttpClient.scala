package finder.domain

import cats._
import cats.effect._
import cats.implicits._
import finder.model.{ InvalidUri, Url }
import org.http4s.EntityDecoder._
import org.http4s.Method._
import org.http4s._
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import tofu._
import tofu.logging.{ Logging, Logs }
import tofu.syntax.handle._
import tofu.syntax.raise._

trait HttpClient[F[_], Ctx] {
  def get(url: Url): F[Option[Ctx]]
}

class DefaultStringHttpClient[F[_]: Sync: Logging](client: Client[F])(
    implicit
    r1: Raise[F, InvalidUri],
    h1: Handle[F, Throwable]
) extends HttpClient[F, String]
    with Http4sClientDsl[F] {
  override def get(url: Url): F[Option[String]] =
    for {
      uri <- Uri
              .fromString(url.value)
              .leftMap(_ => InvalidUri(s"Cannot construct Uri from Url ${url.value}"))
              .toRaise[F]
      req <- GET(uri)
      ctx <- client.expectOption[String](req).handleWith {
              _: Throwable => Logging[F].error(s"Could not reach ${url.value}") *> none[String].pure[F]
            }
    } yield ctx
}

object HttpClient {
  def makeStringHttpClient[I[_]: Functor, F[_]: Sync](
      client: Client[F]
  )(
      implicit
      r1: Raise[F, InvalidUri],
      h1: HandleTo[F, F, ConnectionFailure],
      logs: Logs[I, F]
  ): I[DefaultStringHttpClient[F]] =
    logs.forService[HttpClient[F, String]].map(implicit l => new DefaultStringHttpClient[F](client))
}

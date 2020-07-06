package finder.domain

import cats.effect.Sync
import cats.implicits._
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderException
import tofu._
import tofu.syntax.raise._

trait ConfigLoader[F[_], A] {
  def load: F[A]
}

final class DefaultConfigLoader[F[_]: Sync, A](
    implicit
    raise: Raise[F, ConfigReaderException[A]],
    reader: Derivation[ConfigReader[A]]
) extends ConfigLoader[F, A] {
  override def load: F[A] =
    Sync[F].delay {
      ConfigSource.default.load[A]
    } >>= (_.leftMap(ConfigReaderException.apply).toRaise[F])
}

object ConfigLoader {
  def makeDefaultConfigLoader[F[_]: Sync, A](
      implicit
      raise: Raise[F, ConfigReaderException[A]],
      reader: Derivation[ConfigReader[A]]
  ): F[ConfigLoader[F, A]] =
    Sync[F].delay(new DefaultConfigLoader[F, A])
}

package finder

import cats._
import cats.effect._
import cats.implicits._
import finder.domain._
import finder.model.ApplicationConfig
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.generic.auto._
import tofu.logging.Logs

import scala.concurrent.ExecutionContext

final class DefaultApplicationContextProvider[F[_]: Monad: ContextShift: ConcurrentEffect: Parallel]
    extends ApplicationContextProvider[F] {
  override def ctx: Resource[F, ApplicationContext[F]] =
    BlazeClientBuilder[F](ExecutionContext.global).resource.flatMap { client =>
      Resource.make {
        implicit val logs: Logs[F, F] = Logs.sync[F, F]
        for {
          cfgLoader <- ConfigLoader.makeDefaultConfigLoader[F, ApplicationConfig]
          fileReaderWriter <- FileReaderWriter.makeStringReader[F]
          keywordLoader <- KeywordLoader.makeDefaultKeywordLoader[F](fileReaderWriter)
          httpClient <- HttpClient.makeStringHttpClient[F, F](client)
          counter = KeywordCounter.make[F]
          urlProcessor <- UrlProcessor.make[F, F](httpClient, counter)
          worker <- UrlProcessingWorker.make[F](fileReaderWriter, 100)
        } yield ApplicationContext(cfgLoader, keywordLoader, urlProcessor, worker)
      } { _ =>
        Monad[F].unit
      }
    }

}

object impl {

  import Application._

  implicit def deriveDefaultContextProvider[F[_]: Monad: ContextShift: ConcurrentEffect: Parallel]
      : DefaultApplicationContextProvider[F] =
    new DefaultApplicationContextProvider[F]
}

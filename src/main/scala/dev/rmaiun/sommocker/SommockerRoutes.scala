package dev.rmaiun.sommocker

import cats.{Applicative, Monad}
import cats.effect.{Concurrent, Sync}
import cats.implicits._
import dev.rmaiun.sommocker.dtos.{ConfigurationDataDto, ConfigurationKeyDto}
import dev.rmaiun.sommocker.services.RequestProcessor
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import dev.rmaiun.sommocker.dtos.ConfigurationKeyDto._
import dev.rmaiun.sommocker.dtos.ConfigurationDataDto._
object SommockerRoutes {

  implicit def entityEncoder[F[_] : Applicative, T: Encoder]: EntityEncoder[F, T] = jsonEncoderOf[F, T]
  implicit def entityDecoder[F[_]: Concurrent, T: Decoder]: EntityDecoder[F, T] = jsonOf[F, T]
  def initMock[F[_]: Monad:Concurrent](rp:RequestProcessor[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case request @ POST -> Root / "initMock" =>
        for {
          dto <- request.as[ConfigurationDataDto]
          result <- rp.storeRequestConfiguration(dto)
          resp <- Ok(result)
        } yield resp
    }
  }

  def evaluateMock[F[_] : Monad : Concurrent](rp: RequestProcessor[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case request@POST -> Root / "evaluateMock" =>
        for {
          dto <- request.as[ConfigurationKeyDto]
          result <- rp.invokeRequest(dto)
          resp <- Ok(result)
        } yield resp
    }
  }
}
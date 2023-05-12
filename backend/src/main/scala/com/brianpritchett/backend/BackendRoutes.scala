package com.brianpritchett.backend

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.Uri
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.ValidatingQueryParamDecoderMatcher
import com.brianpritchett.backend.Titlebot.TitlebotError

object BackendRoutes:

  given QueryParamDecoder[Uri] =
    QueryParamDecoder[String].map((uri: String) => Uri.unsafeFromString(uri))

  object UriQueryParamMatcher
      extends ValidatingQueryParamDecoderMatcher[Uri]("uri")

  def routes[F[_]: Sync](T: Titlebot[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "title_favicon" :? UriQueryParamMatcher(
            uriValidated
          ) =>
        uriValidated.fold(
          parseFailures => BadRequest("unable to parse uri"),
          uri =>
            T.get(uri)
              .flatMap { titleFavicon => Ok(titleFavicon) }
              .recoverWith { case TitlebotError(_) =>
                InternalServerError("error retrieving title and favicon")
              }
        )
    }

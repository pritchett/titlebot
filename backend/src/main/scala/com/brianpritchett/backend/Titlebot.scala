package com.brianpritchett.backend

import org.http4s.Uri
import cats.effect.Concurrent
import cats.implicits._
import io.circe.{Encoder, Decoder}
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
import org.http4s.Method._
import java.io.File
import org.http4s.Uri.Path.Segment
import cats.NonEmptyParallel
import org.log4s.getLogger
import cats.effect.kernel.Ref
import java.util.Base64

trait Titlebot[F[_]]:
  def get(url: Uri): F[Titlebot.TitleFavicon]

object Titlebot:
  def apply[F[_]](using ev: Titlebot[F]): Titlebot[F] = ev

  final case class FaviconPath(path: Vector[Segment])

  final case class TitleWithFaviconPath(
      title: Option[String],
      faviconPath: Option[FaviconPath]
  )
  object TitleWithFaviconPath:

    given [F[_]: Concurrent]: EntityDecoder[F, TitleWithFaviconPath] =
      val titleRegex = "<[tT][iI][tT][lL][eE]>(.*)</[tT][iI][tT][lL][eE]>".r
      val faviconPathRegex =
        "<[lL][iI][nN][kK].*[rR][eE][lL].*[hH][eE][rR][fF]=\"(.*)\"".r

      EntityDecoder.decodeBy(MediaType.text.plain) { (msg: Media[F]) =>
        DecodeResult.success(EntityDecoder.decodeText[F](msg).map { body =>
          val title =
            titleRegex.findFirstMatchIn(body).map { m => m.group(1) }
          val faviconPath = faviconPathRegex.findFirstMatchIn(body).map { m =>
            val segments = m.group(1).split("/").toVector.map(Segment(_))
            FaviconPath(segments)
          }
          TitleWithFaviconPath(title, faviconPath)
        })
      }

  final case class TitleFavicon(
      title: Option[String],
      favicon: Option[String]
  )
  object TitleFavicon:
    given Encoder[TitleFavicon] = Encoder.AsObject.derived[TitleFavicon]
    given [F[_]]: EntityEncoder[F, TitleFavicon] = jsonEncoderOf

  final case class TitlebotError(e: Throwable) extends RuntimeException

  def impl[F[_]: Concurrent: NonEmptyParallel](C: Client[F]): Titlebot[F] =
    new Titlebot[F]:
      val dsl = new Http4sClientDsl[F] {}
      import dsl._
      def get(uri: Uri): F[Titlebot.TitleFavicon] = {
        val titleWithFaviconPath = C
          .expect[TitleWithFaviconPath](GET(uri))
          .adaptError { case t => TitlebotError(t) }

        val result = for {
          titleWithPath <- titleWithFaviconPath
          path = titleWithPath.faviconPath
            .map(_.path)
            .getOrElse(Vector(Segment("favicon.ico")))
          faviconUri = uri.withPath(Uri.Path(path))
          favicon <- C
            .expect[Array[Byte]](GET(faviconUri))
            .map(_.some)
            .recover { case _ => None }
        } yield TitleFavicon(
          titleWithPath.title,
          favicon.map(f => Base64.getEncoder.encodeToString(f))
        )

        result
          .adaptError { case t =>
            TitlebotError(t)
          }
      }

  def cachingImpl[F[_]: Concurrent: NonEmptyParallel](
      T: Titlebot[F]
  ): F[Titlebot[F]] =
    Ref.of(Map.empty[Uri, Titlebot.TitleFavicon]).map { cache =>
      new Titlebot[F]:
        def get(uri: Uri): F[Titlebot.TitleFavicon] =
          cache.get
            .flatMap { map =>
              map.get(uri).map(_.pure[F]).getOrElse(T.get(uri))
            }
            .flatTap { tf => cache.update(_.updated(uri, tf)) }
    }

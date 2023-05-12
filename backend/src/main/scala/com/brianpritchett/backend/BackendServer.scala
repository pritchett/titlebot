package com.brianpritchett.backend

import cats.effect.Async
import cats.syntax.all._
import cats.effect.kernel.syntax.all.effectResourceOps
import com.comcast.ip4s._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.client.middleware.FollowRedirect
import cats.NonEmptyParallel

object BackendServer:

  def run[F[_]: Async: NonEmptyParallel]: F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build
      // Many major websites redirect from the beginning, which doesn't provide
      // a good experience if an error is returned for them.
      followRedirectClient = FollowRedirect[F](1)(client)
      titleBotAlg = Titlebot.impl[F](followRedirectClient)
      cachingTitleBotAlg <- Titlebot.cachingImpl(titleBotAlg).toResource

      httpApp = (BackendRoutes.routes[F](cachingTitleBotAlg)).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever

package com.brianpritchett.backend

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.http4s.ember.client.EmberClientBuilder
import munit.CatsEffectSuite

class BackendSpec extends CatsEffectSuite:

  test("Titlebot returns status code 200") {
    assertIO(retHelloWorld.map(_.status), Status.Ok)
  }

  test("Titlebot returns message") {
    assertIO(
      retHelloWorld.flatMap(_.as[String]),
      """{"title":"Brian Pritchett","favicon":null}"""
    )
  }

  private[this] val retHelloWorld: IO[Response[IO]] =
    val getHW =
      Request[IO](
        Method.GET,
        uri"/title_favicon?uri=https://brianpritchett.com"
      )
    EmberClientBuilder.default[IO].build.use { client =>
      val titleBot = Titlebot.impl[IO](client)
      BackendRoutes.routes(titleBot).orNotFound(getHW)
    }

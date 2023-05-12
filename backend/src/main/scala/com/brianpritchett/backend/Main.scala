package com.brianpritchett.backend

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp.Simple:
  val run = BackendServer.run[IO]

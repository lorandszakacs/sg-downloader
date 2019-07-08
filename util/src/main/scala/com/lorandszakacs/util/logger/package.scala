package com.lorandszakacs.util

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  *
  */
package object logger {
  final type Logger[F[_]] = io.chrisdavenport.log4cats.SelfAwareStructuredLogger[F]
  final val Logger: io.chrisdavenport.log4cats.slf4j.Slf4jLogger.type = io.chrisdavenport.log4cats.slf4j.Slf4jLogger
}

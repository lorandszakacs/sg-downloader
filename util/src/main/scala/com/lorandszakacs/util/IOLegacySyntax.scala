package com.lorandszakacs.util

import cats.effect.IO

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  *
  */
trait IOLegacySyntax {
  implicit final def ioUnsafeGetSyntaxOps[T](t: IO[T]): IOLegacySyntax.IOUnsafeGetOps[T] =
    new IOLegacySyntax.IOUnsafeGetOps[T](t)
}

object IOLegacySyntax {
  final class IOUnsafeGetOps[T](val t: IO[T]) extends AnyVal {
    implicit def unsafeSyncGet(): T = t.unsafeRunSync()
  }
}

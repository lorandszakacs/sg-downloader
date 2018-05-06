package com.lorandszakacs.util.time_wrappers

import java.{time => jt}

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 06 May 2018
  *
  */
object StaticDateTimeFormatter extends StaticDateTimeFormatter

trait StaticDateTimeFormatter {

  @inline def ofPattern(pattern: String): jt.format.DateTimeFormatter = jt.format.DateTimeFormatter.ofPattern(pattern)

}

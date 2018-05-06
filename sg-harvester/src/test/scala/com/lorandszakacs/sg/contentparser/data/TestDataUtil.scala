package com.lorandszakacs.sg.contentparser.data

import com.lorandszakacs.util.time._

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 06 May 2018
  *
  */
trait TestDataUtil {

  def unsafeCurrentYear(): Int = Year.unsafeNow().getValue
}

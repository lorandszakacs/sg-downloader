package com.lorandszakacs.sg.exporter.indexwriter.impl

import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 19 Jul 2016
  *
  */
class FileUtilsTest extends FlatSpec {

  behavior of "FileUtils.fileMatchInEverythingButDate"

  it should "... match files where everything is the same but the date" in {
    val s1 = "/home/lorand/Dropbox/Public/suicide-girls/models/favorites/porcelinna/porcelinna_2016-06-11_PEACH_BLOSSOM.html"
    val s2 = "/home/lorand/Dropbox/Public/suicide-girls/models/favorites/porcelinna/porcelinna_2016-07-19_PEACH_BLOSSOM.html"

    assert(FileUtils.fileMatchInEverythingButDate(s1, s2), "... file should have matched")
  }

  it should "... not match files where anything other than the date is not equal" in {
    val s1 = "/home/lorand/Dropbox/Public/suicide-girls/models/favorites/porcelinna/porcelinna_2016-06-11_PEACH_BLOSSOM.html"
    val s2 = "/home/lorand/Dropbox/Public/suicide-girls/models/favorites/porcelinna/porcelinna_2014-12-29_ART_SCHOOL_CONFIDENTIAL.html"

    assert(!FileUtils.fileMatchInEverythingButDate(s1, s2), "... file should not have matched")
  }

}

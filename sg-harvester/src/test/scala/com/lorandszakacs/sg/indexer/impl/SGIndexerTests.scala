package com.lorandszakacs.sg.indexer.impl

import com.lorandszakacs.sg.core
import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.M.{HFFactory, SGFactory}
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.time._
import org.scalatest.Outcome

/**
  *
  * This test requires an active internet connection,
  * since it fetches live data!
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
class SGIndexerTests extends IndexerTest {

  //===============================================================================================
  //===============================================================================================

  behavior of "SGIndexer.getPhotoSetUris"

  //===============================================================================================
  //===============================================================================================

  implicit private class TestInterop[T](value: Task[T]) {
    def r: T = value.unsafeSyncGet()
  }

  /**
    * at the time of writing of this test:
    * $domain/members/dalmasca/photos/
    * has only 4 sets
    *
    * N.B. these can actually change through time.
    */
  it should "... fetch URIs for a page that does not need a subsequent query -- dalmasca" in { indexer =>
    val h: HF = indexer.gatherPhotoSetInformationForM(HFFactory)(Name("dalmasca")).r

    val sets: List[PhotoSet] = h.photoSets
    withClue("size") {
      sets should have size 4
    }

    withClue("content") {
      sets should contain {
        PhotoSet(
          url     = s"${core.Domain}/members/dalmasca/album/996562/picker-uppers/",
          title   = "PICKER-UPPERS",
          date    = LocalDate.parse("2013-03-22"),
          isHFSet = Some(true)
        )
      }
    }
  }

  it should "... fetch URIs for a page that does not need a subsequent query -- dalmasca -- generic" in { indexer =>
    val h: M = indexer.gatherPhotoSetInformationForName(Name("dalmasca")).r
    h shouldBe a[HF]
    val sets: List[PhotoSet] = h.photoSets

    withClue("size") {
      sets should have size 4
    }

    withClue("content") {
      sets should contain {
        PhotoSet(
          url     = s"${core.Domain}/members/dalmasca/album/996562/picker-uppers/",
          title   = "PICKER-UPPERS",
          date    = LocalDate.parse("2013-03-22"),
          isHFSet = Some(true)
        )
      }

    }
  }

  //===============================================================================================
  //===============================================================================================

  /**
    * at the time of writing of this test:
    * $domain/girls/zoli/photos/
    * had 22 sets. And has not published a new set in ages.
    */
  it should "... fetch URIs for a page that needs several queries -- zoli" in { indexer =>
    val sg: SG = indexer.gatherPhotoSetInformationForM(SGFactory)(Name("zoli")).r

    val sets: List[PhotoSet] = sg.photoSets

    withClue("... size") {
      sets should have size 22
    }

    withClue("... content") {
      sets should contain {
        PhotoSet(
          url   = s"${core.Domain}/girls/zoli/album/996153/lounge-act/",
          title = "lounge act",
          date  = LocalDate.parse("2012-10-17")
        )
      }

      sets should contain {
        PhotoSet(
          url   = s"${core.Domain}/girls/zoli/album/969351/the-beat/",
          title = "THE BEAT",
          date  = LocalDate.parse("2006-05-03")
        )
      }
    }
  }

  /**
    * at the time of writing of this test:
    * $domain/girls/zoli/photos/
    * had 22 sets. And has not published a new set in ages.
    */
  it should "... fetch URIs for a page that needs several queries -- zoli -- generic" in { indexer =>
    val sg: M = indexer.gatherPhotoSetInformationForName(Name("zoli")).r
    sg shouldBe a[SG]
    val sets: List[PhotoSet] = sg.photoSets

    withClue("... size") {
      sets should have size 22
    }

    withClue("... content") {
      sets should contain {
        PhotoSet(
          url   = s"${core.Domain}/girls/zoli/album/996153/lounge-act/",
          title = "lounge act",
          date  = LocalDate.parse("2012-10-17")
        )
      }

      sets should contain {
        PhotoSet(
          url   = s"${core.Domain}/girls/zoli/album/969351/the-beat/",
          title = "THE BEAT",
          date  = LocalDate.parse("2006-05-03")
        )
      }
    }

  }

  //===============================================================================================
  //===============================================================================================

  behavior of "SGIndexer.gatherSGNames"

  //===============================================================================================
  //===============================================================================================

  /**
    * It's important to keep in mind that since this is live data that is being fetched,
    * this test might fail. Therefore one must always be vigilant.
    */
  it should "... gather the first 48 SG names by followers" in { indexer =>
    val names: List[Name] = indexer.gatherSGNames(48).r
    withClue("... size") {
      names should have size 48
    }

    print {
      s"""
         |SG names:
         |${names.mkString("\n")}
         |
        """.stripMargin
    }

    withClue("... content") {
      names should contain("Sash".toName)
      names should contain("Kemper".toName)
      names should contain("Gogo".toName)
    }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 HF names by followers" in { indexer =>
    val names: List[Name] = indexer.gatherHFNames(48).r
    print {
      s"""
         |HF names:
         |${names.mkString("\n")}
         |
        """.stripMargin
    }

    withClue("... size") {
      names should have size 48
    }
  }

  //===============================================================================================
  //===============================================================================================

  behavior of "SGIndexer.gatherNewestMInformation"

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 24 new sets" in { indexer =>
    val ms: List[M] = indexer.gatherAllNewMsAndOnlyTheirLatestSet(24, None).r
    withClue("... size") {
      ms should have size 24
    }
    withClue("... distribution") {
      assert(ms.exists(_.isHF), "... there should be at least one HF in the past 48 new sets")
      assert(ms.exists(_.isSG), "... there should be at least one SG in the past 48 new sets")
    }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 new sets" in { indexer =>
    val ms: List[M] = indexer.gatherAllNewMsAndOnlyTheirLatestSet(48, None).r
    withClue("... size") {
      ms should have size 48
    }
    withClue("... distribution") {
      assert(ms.exists(_.isHF), "... there should be at least one HF in the past 48 new sets")
      assert(ms.exists(_.isSG), "... there should be at least one SG in the past 48 new sets")
    }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 new sets, then use one in the middle as the latest processed, and return only the ones before it" in {
    indexer =>
      val previousMs = {
        val ms: List[M] = indexer.gatherAllNewMsAndOnlyTheirLatestSet(48, None).r
        withClue("... size") {
          assert(ms.length >= 48)
        }
        withClue("... distribution") {
          assert(ms.exists(_.isHF), "... there should be at least one HF in the past 48 new sets")
          assert(ms.exists(_.isSG), "... there should be at least one SG in the past 48 new sets")
        }
        ms
      }

      println("-------------------------------------")

      withClue("... lastProcessed marker is on 1st page") {
        val index  = 13
        val latest = previousMs(index)
        val lastProcessed: LastProcessedMarker = indexer.createLastProcessedIndex(latest)
        println(s"---> LPM: ${lastProcessed.lastPhotoSetID}")

        withClue("... now gathering only a part of the processed sets") {
          val ms: List[M] = indexer.gatherAllNewMsAndOnlyTheirLatestSet(Int.MaxValue, Option(lastProcessed)).r
          withClue("... size") {
            ms should have size index.toLong
          }
          withClue("... distribution") {
            assert(!ms.exists(_.name == latest.name), "... the latest M should not be in this list")
            assert(ms.exists(_.isHF),                 "... there should be at least one HF in the past 48 new sets")
            assert(ms.exists(_.isSG),                 "... there should be at least one SG in the past 48 new sets")
          }
          ms
        }
      }

      println("-------------------------------------")

      withClue("... lastProcessed marker is on 2nd page") {
        val index  = 28
        val latest = previousMs(index)
        val lastProcessed: LastProcessedMarker = indexer.createLastProcessedIndex(latest)
        println(s"---> LPM: ${lastProcessed.lastPhotoSetID}")

        withClue("... now gathering only a part of the processed sets") {
          val ms: List[M] = indexer.gatherAllNewMsAndOnlyTheirLatestSet(Int.MaxValue, Option(lastProcessed)).r
          withClue("... size") {
            assert(ms.length == index, "... models returned ought to be 28")
          }
          withClue("... distribution") {
            assert(!ms.exists(_.name == latest.name), "... the latest M should not be in this list")
            assert(ms.exists(_.isHF),                 "... there should be at least one HF in the past 48 new sets")
            assert(ms.exists(_.isSG),                 "... there should be at least one SG in the past 48 new sets")
          }
          ms
        }
      }

  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 new sets, then use first one as latest. No subsequent MS should be returned" in {
    indexer =>
      val previousMs = {
        val ms: List[M] = indexer.gatherAllNewMsAndOnlyTheirLatestSet(48, None).r
        withClue("... size") {
          ms should have size 48
        }
        withClue("... distribution") {
          assert(ms.exists(_.isHF), "... there should be at least one HF in the past 48 new sets")
          assert(ms.exists(_.isSG), "... there should be at least one SG in the past 48 new sets")
        }
        ms
      }
      val index  = 0
      val latest = previousMs(index)
      val lastProcessed: LastProcessedMarker = indexer.createLastProcessedIndex(latest)

      withClue("... now gathering only a part of the processed sets") {
        val ms: List[M] = indexer.gatherAllNewMsAndOnlyTheirLatestSet(Int.MaxValue, Option(lastProcessed)).r
        withClue("... size") {
          ms should have size index.toLong
        }
        withClue("... distribution") {
          assert(!ms.exists(_.name == latest.name), "... the latest M should not be in this list")
        }
        ms
      }
  }

  //===============================================================================================
  //===============================================================================================

  override type FixtureParam = SGIndexerImpl

  override protected def withFixture(test: OneArgTest): Outcome = {
    val assembly = new IndexerAssembly with SGClientAssembly {
      implicit override def httpIOScheduler: HTTPIOScheduler = SGIndexerTests.this.httpIOSch
    }

    test.apply(assembly._sgIndexerImpl)
  }
}

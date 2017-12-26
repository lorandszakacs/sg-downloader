package com.lorandszakacs.sg.indexer.impl

import akka.actor.ActorSystem
import com.lorandszakacs.sg.core
import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.M.{HFFactory, SGFactory}
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.future._
import org.joda.time.LocalDate
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

  /**
    * at the time of writing of this test:
    * $domain/members/odina/photos/
    * had only one single set
    */
  it should "... fetch URIs for a page that does not need a subsequent query -- odina" in { indexer =>
    whenReady(indexer.gatherPhotoSetInformationForModel(HFFactory)(Name("odina"))) { h: HF =>
      val sets: List[PhotoSet] = h.photoSets

      withClue("size") {
        sets should have size 1
      }

      withClue("content") {
        sets should contain {
          PhotoSet(
            url     = s"${core.Domain}/members/odina/album/2745718/do-i-wanna-know/",
            title   = "DO I WANNA KNOW",
            date    = LocalDate.parse("2016-07-03"),
            isHFSet = Some(true)
          )
        }
      }

    }
  }

  it should "... fetch URIs for a page that does not need a subsequent query -- odina -- generic" in { indexer =>
    whenReady(indexer.gatherPhotoSetInformationForModel(Name("odina"))) { h: M =>
      h shouldBe a[HF]
      val sets: List[PhotoSet] = h.photoSets

      withClue("size") {
        sets should have size 1
      }

      withClue("content") {
        sets should contain {
          PhotoSet(
            url     = s"${core.Domain}/members/odina/album/2745718/do-i-wanna-know/",
            title   = "DO I WANNA KNOW",
            date    = LocalDate.parse("2016-07-03"),
            isHFSet = Some(true)
          )
        }
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
    whenReady(indexer.gatherPhotoSetInformationForModel(SGFactory)(Name("zoli"))) { sg: SG =>
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
  }

  /**
    * at the time of writing of this test:
    * $domain/girls/zoli/photos/
    * had 22 sets. And has not published a new set in ages.
    */
  it should "... fetch URIs for a page that needs several queries -- zoli -- generic" in { indexer =>
    whenReady(indexer.gatherPhotoSetInformationForModel(Name("zoli"))) { sg: M =>
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
    whenReady(indexer.gatherSGNames(48)) { names: List[Name] =>
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
        names should contain("Sash".toModelName)
        names should contain("Kemper".toModelName)
        names should contain("Gogo".toModelName)
      }

    }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 HF names by followers" in { indexer =>
    whenReady(indexer.gatherHFNames(48)) { names: List[Name] =>
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
  }

  //===============================================================================================
  //===============================================================================================

  behavior of "SGIndexer.gatherNewestModelInformation"

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 new sets" in { indexer =>
    whenReady(indexer.gatherAllNewMsAndOnlyTheirLatestSet(48, None)) { ms: List[M] =>
      withClue("... size") {
        ms should have size 48
      }
      withClue("... distribution") {
        assert(ms.exists(_.isHF), "... there should be at least one HF in the past 48 new sets")
        assert(ms.exists(_.isSG), "... there should be at least one SG in the past 48 new sets")
      }
    }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 new sets, then use one in the middle as the latest processed, and return only the ones before it" in {
    indexer =>
      val previousMs = whenReady(indexer.gatherAllNewMsAndOnlyTheirLatestSet(48, None)) { ms: List[M] =>
        withClue("... size") {
          ms should have size 48
        }
        withClue("... distribution") {
          assert(ms.exists(_.isHF), "... there should be at least one HF in the past 48 new sets")
          assert(ms.exists(_.isSG), "... there should be at least one SG in the past 48 new sets")
        }
        ms
      }
      val index  = 13
      val latest = previousMs(index)
      val lastProcessed: LastProcessedMarker = indexer.createLastProcessedIndex(latest)

      withClue("... now gathering only a part of the processed sets") {
        whenReady(indexer.gatherAllNewMsAndOnlyTheirLatestSet(48, Option(lastProcessed))) { ms: List[M] =>
          withClue("... size") {
            ms should have size index.toLong
          }
          withClue("... distribution") {
            assert(!ms.exists(_.name == latest.name), "... the latest model should not be in this list")
            assert(ms.exists(_.isHF), "... there should be at least one HF in the past 48 new sets")
            assert(ms.exists(_.isSG), "... there should be at least one SG in the past 48 new sets")
          }
          ms
        }
      }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 new sets, then use first one as latest. No subsequent MS should be returned" in {
    indexer =>
      val previousMs = whenReady(indexer.gatherAllNewMsAndOnlyTheirLatestSet(48, None)) { ms: List[M] =>
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
        whenReady(indexer.gatherAllNewMsAndOnlyTheirLatestSet(48, Option(lastProcessed))) { ms: List[M] =>
          withClue("... size") {
            ms should have size index.toLong
          }
          withClue("... distribution") {
            assert(!ms.exists(_.name == latest.name), "... the latest model should not be in this list")
          }
          ms
        }
      }
  }

  //===============================================================================================
  //===============================================================================================

  override type FixtureParam = SGIndexerImpl

  override protected def withFixture(test: OneArgTest): Outcome = {
    val assembly = new IndexerAssembly with SGClientAssembly {
      override implicit def actorSystem: ActorSystem = SGIndexerTests.this.actorSystem

      override implicit def executionContext: ExecutionContext = SGIndexerTests.this.ec
    }

    test.apply(assembly._sgIndexerImpl)
  }
}

package com.lorandszakacs.sg.app.repl

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
@scala.deprecated("use the ones app.commands which are parsed", "now")
object Commands {

  case class Command(
    id: String,
    description: String
  ) {
    def sameId(otherId: String): Boolean = {
      otherId.trim().toLowerCase == this.id
    }
  }


  lazy val DeltaUpdate: Command = Command(
    "delta",
    s"""
       |Fetches latest information from the website, and updates that which needs updating.
       |Composite of ${GatherNew.id} and ${IndexNew.id} and exporting of HTML.
    """.stripMargin
  )

  lazy val ShowModel: Command = Command(
    "show [model-name]",
    """
      |Shows all information about a model.
    """.stripMargin
  )

  lazy val Help: Command = Command(
    "help",
    "help"
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val ReindexHopefuls: Command = Command(
    "reindex-hopefuls",
    """
      |reindex all hopefuls. Harvests from scratch all available hopefuls at:
      |https://www.suicidegirls.com/profiles/hopeful/followers/
    """.stripMargin
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val ReindexSuicideGirls: Command = Command(
    "reindex-suicide-girls",
    """
      |reindex all suicide girls. Harvests from scratch all available suicide girls at:
      |https://www.suicidegirls.com/profiles/girl/followers/
    """.stripMargin
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val ReindexAll: Command = Command(
    "reindex-all",
    s"""
       |Composite operation of ${ReindexHopefuls.id} and ${ReindexSuicideGirls.id}
    """.stripMargin
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val GatherNew: Command = Command(
    "gather-new",
    """
      |harvest and reindex new entires. Will mark for reindexing all SGs and hopefuls that were created, from:
      |https://www.suicidegirls.com/photos/all/recent/all/
    """.stripMargin
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val IndexNew: Command = Command(
    "index-new",
    """
      |gather all set information for the suicidegirls, and hopefuls marked as to index.
      |This requires authentication since it goes on the pages of each model.
    """.stripMargin
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val GatherAndIndexAll: Command = Command(
    "update-and-reindex-all",
    s"""
       |Gather information about ALL existing suicidegirls and hopefuls, and reparses all their data from the website
    """.stripMargin
  )


  @scala.deprecated("needs to be renamed", "now")
  lazy val UpdateSpecific: Command = Command(
    "update-specific",
    s"""
       |Fetches latest information from the website, and updates that which needs updating.
       |Composite of gathering information about a specific model, and then index, and then exporting html
    """.stripMargin
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val CleanIndex: Command = Command(
    "index-clean",
    """
      |clean all models from index that have zero sets on the website
    """.stripMargin
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val GenerateNewest: Command = Command(
    "newest",
    """
      |generates html containing newest sg-s for the past two months
    """.stripMargin
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val DetectDuplicateFiles: Command = Command(
    "detect-duplicate-files",
    """
      |if you keep applying delta upgrades to html. You can find a set that was not on FP, suddenly be on FP, in that case the
      |date of the set changes. So you will wind up with two html files. This command will attempt to find such duplicates, and
      |remove them manually. There are some known false alarms. They are filtered out manually
      | """.stripMargin
  )



  @scala.deprecated("needs to be renamed", "now")
  lazy val HtmlFavorites: Command = Command(
    "html-favorites",
    "\nexports a navigable html page of all favorite models @ ~/sgs/local/models/favorites\n"
  )

  @scala.deprecated("needs to be renamed", "now")
  lazy val HtmlAll: Command = Command(
    "html-all",
    "\nexports a navigable html page of all models @ ~/sgs/local/models/all\n"
  )

  lazy val Exit: Command = Command(
    "exit",
    "\nexit.\n"
  )

  lazy val DisplayFavorites: Command = Command(
    "favorites",
    "\ndisplay favorites\n"
  )

  lazy val all: List[Command] = List(
    Exit,
    ReindexHopefuls,
    ReindexSuicideGirls,
    ReindexAll,
    GatherNew,
    IndexNew,
    CleanIndex,
    DetectDuplicateFiles,
    GatherAndIndexAll,
    DeltaUpdate,
    ShowModel,
    HtmlFavorites,
    HtmlAll,
    DisplayFavorites,
    UpdateSpecific
  ).sortBy(_.id)
}

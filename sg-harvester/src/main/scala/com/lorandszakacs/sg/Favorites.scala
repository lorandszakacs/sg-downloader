package com.lorandszakacs.sg

import com.lorandszakacs.sg.model.ModelName

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines, lsz@lorandszakacs.com
  * @since 12 Jul 2016
  *
  */
object Favorites {

  private lazy val _modelsNames = List(
    "aeterna",
    "ajilee",
    "annalee",
    "bea",
    "bob",
    "chad",
    "clareon",
    "coralinne",
    "damsel",
    "delacour",
    "delora",
    "discordia",
    "eden",
    "eleena",
    "elliemouse",
    "episkey",
    "evette",
    "exning",
    "fay",
    "feryn",
    "fishball",
    "gogo",
    "hind",
    "idony",
    "jackye",
    "jamity",
    "janesinner",
    "kaotika",
    "katherine",
    "kemper",
    "ladyviolet",
    "lillianrose",
    "liu",
    "lumo",
    "marajade",
    "maud",
    "mel",
    "mille",
    "milloux",
    "moon",
    "natashalegeyda",
    "ness",
    "ondinae",
    "pandub",
    "patton",
    "piedpiper",
    "plum",
    "porcelinna",
    "riae",
    "riversong",
    "salliss",
    "saria",
    "sashabrink",
    "sedona",
    "shannon_",
    "skyhook",
    "slim",
    "spliff",
    "stormyent",
    "talena",
    "ultima",
    "venom",
    "yugen",
    "yuxi",
    "zenit",
    "zoli"
  )

  val modelNames = _modelsNames.map(ModelName.apply)

  def codeFriendlyDisplay: String = _modelsNames.mkString("""List("""", s"""",${"\n"}"""", """")""")
}

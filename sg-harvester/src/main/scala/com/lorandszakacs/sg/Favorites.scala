package com.lorandszakacs.sg

import com.lorandszakacs.sg.model.ModelName

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines, lsz@lorandszakacs.com
  * @since 12 Jul 2016
  *
  */
object Favorites {

  private lazy val _modelsNames =
    List(
      "aeterna", "ailiqueen", "aisline", "ajilee", "aleli", "alicesey", "alle", "almendra", "anabuhr", "annalee",
      "annasthesia", "antigone", "arabella", "arielscout", "arwen", "astraia", "atomic", "aymi", "bae", "bambu",
      "bandit", "bea", "bob", "brewin", "brunabruce", "bully", "cadorna", "candyhell", "carrina", "cartoon",
      "casanova", "cecylia", "chad", "charm", "chelss", "cheri", "chrisrayn", "circa", "clareon", "cleoo",
      "coralinne", "dali", "damsel", "delacour", "delora", "demonia", "dimples", "discordia", "dwam", "ecco",
      "eden", "effy", "einiss", "eleena", "ellie", "elliemouse", "elliott", "episkey", "equateur", "evette",
      "exning", "fay", "feryn", "fishball", "flux", "fridah", "glitch", "gogo", "gypsyy", "heathen",
      "hexe", "hind", "idony", "illusion", "indi", "jackye", "jamity", "janedoe", "janesinner", "janette",
      "jessicalou", "joker", "july", "kacy", "kaotika", "katherine", "kemper", "kirbee", "kush", "ladyviolet",
      "lait", "lalka", "lanei", "lass", "lavonne", "lego", "lillianrose", "liu", "luana", "lucid",
      "lumo", "lunar", "luxlee", "lylie", "lyvia", "lyxzen", "machete", "magpie", "mah", "manko",
      "maokiz", "marajade", "margarette", "marlene", "marquise", "maud", "mel", "mendacia", "mermaid", "milenci",
      "mille", "milloux", "mnemozyne", "moon", "morrigan", "natashalegeyda", "nemesis", "ness", "nubia", "okami",
      "ondinae", "oogie", "paloma", "pandie", "pandub", "patton", "pesky", "phoenix", "pia", "piedpiper",
      "pilot", "pirate", "plum", "polaw", "poox", "porcelinna", "porphyria", "posh", "priscila", "prism",
      "prussia", "pulp", "pyke", "rachelle", "radeo", "raleigh", "rambo", "rawruh", "reed", "revenge",
      "rhue", "riae", "ripley", "riversong", "rjen", "rourke", "saint", "salliss", "saralilith", "saria",
      "sashabrink", "satelina", "saturn", "savanna", "scribbles", "sedona", "shamandalie", "shannon_", "shine", "sibila",
      "silencia", "silvi", "sinnah", "sinni", "skinbyrd", "skydot", "skyhook", "slim", "smash", "spliff",
      "spock", "stephy", "sterling_", "stormyent", "sunny", "tabbytha", "talena", "tangerine", "tanyabat", "tarion",
      "tasha", "tchip", "tegnwar", "terrox", "teslaa", "tigh", "titan_", "torasuicide", "totiga", "tristyn",
      "twiitch", "ultima", "ultramarie", "vaniislima", "vareon", "vassilis", "veive", "venom", "verde", "vice",
      "viking", "vivid", "waikiki", "whiskey", "wolf", "yesenia", "yugen", "yuxi", "zad", "zell",
      "zenit", "zephi", "zilvia", "zippo", "zoli"
    ).sorted.distinct

  private val _new = List[String]().filterNot(_.isEmpty).sorted.distinct

  val modelNames = (if (_new.isEmpty) _modelsNames else _new) map ModelName.apply

  def codeFriendlyDisplay: String = (_modelsNames ++ _new).distinct.sorted.grouped(10).map(_.mkString("\"", "\", \"", "\"")).mkString("List(\n", ",\n", "\n)")
}

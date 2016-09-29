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
      "aeterna", "ailiqueen", "aisline", "ajilee", "aleli", "alerosebunny", "alicesey", "alicesudos", "alle", "almendra",
      "anabuhr", "aniston", "annalee", "annasthesia", "antigone", "arabella", "arielscout", "arwen", "astraia", "atomic",
      "aubrey", "avrora", "aymi", "bae", "bambu", "bandit", "bea", "beckah", "bluedette", "bob",
      "brewin", "brunabruce", "bully", "cadorna", "candyhell", "carrina", "cartoon", "casanova", "cecylia", "chad",
      "charm", "chelss", "cheri", "cherrybuttonz", "chrisrayn", "circa", "clareon", "cleoo", "coralinne", "dali",
      "damsel", "delacour", "delora", "demonia", "devilins", "dimarti", "dimples", "discordia", "dollyd", "dwam",
      "ecco", "eden", "effy", "einiss", "eleena", "elisarios", "elisarose", "ellie", "elliemouse", "elliott",
      "emmameow", "episkey", "equateur", "evette", "exning", "fay", "feryn", "fishball", "flux", "fridah",
      "gallows", "glitch", "gogo", "gypsyy", "haven", "heathen", "helenahopper", "hexe", "hildegarde", "hind",
      "hylia", "idony", "illusion", "indi", "jackye", "jamity", "janedoe", "janesinner", "janette", "jessicalou",
      "joker", "july", "kacy", "kala", "kaotika", "karanlit", "katherine", "kaylindear", "kemper", "kirbee",
      "kush", "ladyviolet", "lait", "lalka", "lanei", "lass", "lavonne", "lego", "lillianrose", "lilymai",
      "lilyt", "liu", "livay", "liya", "luana", "lucerne", "lucid", "lumo", "lunar", "luxlee",
      "lylie", "lyvia", "lyxzen", "machete", "magpie", "mah", "manko", "maokiz", "marajade", "margarette",
      "marialauriejupiter", "marlene", "marquise", "mars", "maud", "mel", "melbee", "mendacia", "mermaid", "milenci",
      "mille", "milloux", "mistica", "mnemozyne", "moon", "morrigan", "natashalegeyda", "nattybohh", "nemesis", "ness",
      "nubia", "okami", "ondinae", "oogie", "paloma", "pandie", "pandub", "patton", "pesky", "phoenix",
      "pia", "piedpiper", "pilot", "pirate", "plum", "polaw", "poox", "porcelinna", "porphyria", "posh",
      "priscila", "prism", "prussia", "psyco_", "pulp", "pyke", "queenblossom", "rachelle", "radeo", "raleigh",
      "rambo", "rawruh", "reed", "revenge", "rhue", "riae", "ripley", "riversong", "rjen", "rourke",
      "saint", "salliss", "saralilith", "saria", "sashabrink", "satelina", "satin", "saturn", "savanna", "scribbles",
      "sedona", "shamandalie", "shannon_", "shaunanyx", "shine", "shotgun", "sibila", "silencia", "silvi", "sinnah",
      "sinni", "skinbyrd", "skydot", "skyhook", "slim", "smash", "spliff", "spock", "stephy", "sterling_",
      "stormyent", "sugarkane90", "sunny", "tabbytha", "talena", "tangerine", "tanyabat", "tarion", "tasha", "tchip",
      "tegnwar", "terrox", "teslaa", "tigh", "titan_", "torasuicide", "totiga", "tristyn", "twiitch", "ultima",
      "ultramarie", "vaniislima", "vareon", "vassilis", "veive", "venom", "verde", "vice", "viking", "vivid",
      "waikiki", "whiskey", "wilhelmine", "wolf", "yesenia", "yugen", "yusuracircus", "yuxi", "zad", "zell",
      "zenit", "zephi", "zilvia", "zippo", "zoli"
    ).sorted.distinct

  private val _new = List[String]().filterNot(_.isEmpty).sorted.distinct

  val modelNames = (if (_new.isEmpty) _modelsNames else _new) map ModelName.apply

  def codeFriendlyDisplay: String = (_modelsNames ++ _new)
    .map(ModelName.apply).map(_.name).distinct.sorted.grouped(10)
    .map(_.mkString("\"", "\", \"", "\"")).mkString("List(\n", ",\n", "\n)")
}

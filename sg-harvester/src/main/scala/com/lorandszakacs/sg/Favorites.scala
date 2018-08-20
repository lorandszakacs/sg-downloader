package com.lorandszakacs.sg

import com.lorandszakacs.sg.model.Name

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines, lsz@lorandszakacs.com
  * @since 12 Jul 2016
  *
  */
object Favorites {

  // format: off
  private lazy val _names =
    List(
      "adeli", "adorn", "aeluna", "aerie", "aeterna", "agatha", "ailiqueen", "ainoa", "aisline", "ajilee",
      "akemi", "akilina", "akiramai", "aleli", "alenagerman", "alerose", "alerosebunny", "alicelise", "alicesey", "alicesudos",
      "alienn", "alle", "almendra", "amandaellen", "amandafayeest", "amarna", "amity_", "anabuhr", "ancalagon", "angellima",
      "anikabonny", "aniston", "annabatman", "annalee", "annasthesia", "anoukevil", "antaje", "antigone", "aonbheannach", "arabella",
      "arielscout", "arriane", "arsenica", "arwen", "asami", "astraia", "atomic", "attycus", "aubrey", "aurorah",
      "aurralee", "avrora", "aymi", "azalea", "azulcielo", "bae", "bambu", "bandit", "barbsoares", "baxter_",
      "bea", "beckah", "blackashpanther", "blair", "blink", "bloodyjade", "blue", "bluedette", "bob", "bobonco",
      "booba", "borquita", "bourbon", "breenda", "brewin", "brisen", "bruna", "brunabruce", "brunah", "brynx",
      "bully", "cadorna", "calalini", "candyhell", "carderon", "carrina", "cartoon", "casanova", "catarina", "cecylia",
      "celine", "ceres", "ceriz", "chad", "chaosdream", "charm", "chelss", "cheri", "cherrybuttonz", "chrisrayn",
      "circa", "clareon", "clemm8", "cleoo", "comet", "conrada", "coolicio", "coralinne", "core", "cyberre",
      "cygnet", "dali", "damsel", "damselfly", "danette", "dawud", "debby", "delacour", "delora", "demonia",
      "devilins", "diaigi", "dimarti", "dimples", "discordia", "dojikko", "dollyd", "donyx", "douxreves", "drachen",
      "dread", "drew", "dwam", "earendil", "ecco", "eclipsa", "eden", "effy", "einiss", "eirenne",
      "eldereth", "eleena", "elisa", "elisarios", "elisarose", "ellathrasher", "ellie", "elliehedgie", "elliemouse", "elliott",
      "ellqvist", "emmalou", "emmameow", "episkey", "equateur", "evanesco", "eveel", "evette", "evevalentine", "exning",
      "fay", "fayewhyte", "fennek", "feryn", "fishball", "flux", "frags", "frenia", "fridah", "frosty",
      "gaarnet", "gallows", "gatete", "ghosttkitten", "giuchi", "gladyce", "glameow", "glitch", "gogo", "greie",
      "gypsyy", "haven", "heathen", "hel_", "helen", "helenahopper", "heliane", "herem", "hexe", "hildegarde",
      "hind", "honeybea", "hopeiero", "hylia", "idony", "idrilium", "idunn", "illusion", "ilo", "indaco",
      "indi", "inngrin", "ivory", "ivylina", "jackye", "jam", "jamity", "janedoe", "janesinner", "janette",
      "jef", "jessi", "jessi77", "jessicalou", "joker", "juliju", "july", "kacy", "kadito", "kailah",
      "kala", "kaotika", "karanlit", "katherine", "kaylindear", "kemper", "khalida", "kirbee", "konekoya", "kuroha",
      "kush", "lady", "ladyviolet", "lait", "lalka", "lanei", "larabea", "lascaux", "lass", "latoya",
      "lavonne", "leella", "leelou", "leetattar", "lego", "lelly", "lerahon", "lestis", "lexiexj", "leza",
      "lia_cobain", "lilithdagon", "lillianrose", "lillibayle", "lilymai", "lilyt", "linziebelle", "liryc", "littlecalypso", "liu",
      "livay", "liya", "lizstein", "lockhart", "lolitta", "lollimila", "lovelace", "loveless", "lua", "luana",
      "lucerne", "lucid", "lufae", "lumo", "lunar", "lure", "luxlee", "lylie", "lyn", "lyuba",
      "lyvia", "lyxzen", "machete", "magpie", "mah", "malicatsz", "malika", "malvyna", "mandeelou", "manko",
      "maokiz", "marajade", "margarette", "mari", "mariajupiter", "mariselle", "marlene", "marquise", "mars", "maud",
      "meela", "mel", "melanya", "melbee", "mendacia", "mercury", "mermaid", "mhere", "mialeveret", "miele",
      "milenci", "mille", "milloux", "mimzyy", "missfernandez", "mistica", "mistyy", "mnemozyne", "moninoke", "moon",
      "morena", "morikat", "morrigan", "nadeshda", "narciss", "nataliejanex", "natashalegeyda", "nattybohh", "nebula", "nefka",
      "nemesis", "nery", "ness", "nikkisleepy", "noel", "nubia", "nykki", "nymphony", "octaviamay", "okami",
      "ondinae", "oogie", "orion", "oro", "paigewinters", "paloma", "palomadias", "pandie", "pandub", "patton",
      "paulam", "pawlaraw", "pekopeko", "penny", "pesky", "phoenix", "pia", "pialora", "piedpiper", "pilot",
      "pirate", "plum", "polaw", "poox", "porcelinna", "porphyria", "posh", "priscila", "prism", "prussia",
      "psyco_", "pulp", "pvris", "pyke", "queenblossom", "rachelle", "radeo", "raeriley", "raleigh", "rambo",
      "ramen", "raphaelite", "rawruh", "rebel_line", "rebyt", "redsnow", "reed", "refen", "renycide", "revenge",
      "rhue", "riae", "ripley", "riversong", "rjen", "rockyemerson", "romaica", "rouge", "rourke", "sabbbre",
      "saint", "salliss", "saralilith", "saria", "sashabrink", "satelina", "satin", "satinkaa", "saturn", "savanna",
      "scribbles", "sedona", "sejmet", "selenite", "serenna", "shamandalie", "shannon_", "shaunanyx", "shay", "sheer",
      "shine", "shotgun", "sibila", "siderealbreeze", "siko", "silencia", "silvery", "silvi", "sinnah", "sinni",
      "sivir", "skinbyrd", "skydot", "skyhook", "slim", "smash", "smoky", "snowyfeles", "somer", "sophoulla",
      "soul", "spliff", "spock", "stark", "stephy", "stephyrodriguez", "sterling_", "stormyent", "sua", "sugarkane90",
      "sugarr", "sundew", "sunny", "swann", "tabbytha", "talena", "tangerine", "tanyabat", "tarion", "tasha",
      "tchip", "teenarancore", "tegnwar", "terrorlydia", "terrox", "teslaa", "thaiz", "thatty", "tigh", "tinyphantom",
      "titan_", "torasuicide", "totiga", "trebble", "trece", "tristyn", "twiitch", "ultima", "ultramarie", "undead",
      "uynn", "vaire", "valeryraccoon", "vandoll", "vaniislima", "vanp", "vareon", "vassilis", "vaughn", "vehera",
      "veive", "velour", "venera", "venom", "verde", "vice", "viking", "vikki", "viky", "vivid",
      "waikiki", "wednesdaya", "whiskey", "wichitak", "wilhelmine", "wingsweak", "wolf", "xuxa", "yegola", "yesenia",
      "your_mjsty", "yugen", "yuke", "yuno", "yupa", "yusuracircus", "yuxi", "zad", "zeezee", "zell",
      "zenit", "zephi", "zilvia", "zippo", "zoli"
    ).sorted.distinct
  // format: on

  private val _new = List[String](
    ).filterNot(_.isEmpty).sorted.distinct

  lazy val names: List[Name] = (if (_new.isEmpty) _names else _new) map Name.apply

  lazy val namesSet: Set[Name] = _names.toSet.map(Name.apply)

  def codeFriendlyDisplay: String =
    (_names ++ _new)
      .map(Name.apply)
      .map(_.name)
      .distinct
      .sorted
      .grouped(10)
      .map(_.mkString("\"", "\", \"", "\""))
      .mkString("List(\n", ",\n", "\n)")
}

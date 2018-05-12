package com.lorandszakacs.sg

import com.lorandszakacs.sg.model.Name

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines, lsz@lorandszakacs.com
  * @since 12 Jul 2016
  *
  */
object Favorites {

  private lazy val _names =
    List(
      "adeli",
      "adorn",
      "aeluna",
      "aerie",
      "aeterna",
      "agatha",
      "ailiqueen",
      "ainoa",
      "aisline",
      "ajilee",
      "akilina",
      "akiramai",
      "aleli",
      "alenagerman",
      "alerose",
      "alerosebunny",
      "alicesey",
      "alicesudos",
      "alienn",
      "alle",
      "almendra",
      "amandaellen",
      "amarna",
      "amity_",
      "anabuhr",
      "ancalagon",
      "angellima",
      "anikabonny",
      "aniston",
      "annabatman",
      "annalee",
      "annasthesia",
      "anoukevil",
      "antaje",
      "antigone",
      "aonbheannach",
      "arabella",
      "arielscout",
      "arriane",
      "arwen",
      "asami",
      "astraia",
      "atomic",
      "attycus",
      "aubrey",
      "aurorah",
      "aurralee",
      "avrora",
      "aymi",
      "azalea",
      "azulcielo",
      "bae",
      "bambu",
      "bandit",
      "barbsoares",
      "baxter_",
      "bea",
      "beckah",
      "blackashpanther",
      "blair",
      "blink",
      "bloodyjade",
      "blue",
      "bluedette",
      "bob",
      "booba",
      "bourbon",
      "breenda",
      "brewin",
      "brisen",
      "bruna",
      "brunabruce",
      "brunah",
      "brynx",
      "bully",
      "cadorna",
      "calalini",
      "candyhell",
      "carderon",
      "carrina",
      "cartoon",
      "casanova",
      "catarina",
      "cecylia",
      "celine",
      "ceres",
      "ceriz",
      "chad",
      "chaosdream",
      "charm",
      "chelss",
      "cheri",
      "cherrybuttonz",
      "chrisrayn",
      "circa",
      "clareon",
      "clemm8",
      "cleoo",
      "conrada",
      "coolicio",
      "coralinne",
      "core",
      "cyberre",
      "dali",
      "damsel",
      "damselfly",
      "danette",
      "dawud",
      "delacour",
      "delora",
      "demonia",
      "devilins",
      "diaigi",
      "dimarti",
      "dimples",
      "discordia",
      "dojikko",
      "dollyd",
      "donyx",
      "douxreves",
      "drachen",
      "dread",
      "drew",
      "dwam",
      "earendil",
      "ecco",
      "eclipsa",
      "eden",
      "effy",
      "einiss",
      "eirenne",
      "eleena",
      "elisa",
      "elisarios",
      "elisarose",
      "ellie",
      "elliemouse",
      "elliott",
      "ellqvist",
      "emmalou",
      "emmameow",
      "episkey",
      "equateur",
      "evanesco",
      "eveel",
      "evette",
      "evevalentine",
      "exning",
      "fay",
      "fayewhyte",
      "fennek",
      "feryn",
      "fishball",
      "flux",
      "frags",
      "fridah",
      "frosty",
      "gaarnet",
      "gallows",
      "gatete",
      "ghosttkitten",
      "giuchi",
      "gladyce",
      "glameow",
      "glitch",
      "gogo",
      "greie",
      "gypsyy",
      "haven",
      "heathen",
      "helen",
      "helenahopper",
      "heliane",
      "herem",
      "hexe",
      "hildegarde",
      "hind",
      "honeybea",
      "hylia",
      "idony",
      "idunn",
      "illusion",
      "ilo",
      "indaco",
      "indi",
      "inngrin",
      "ivory",
      "ivylina",
      "jackye",
      "jam",
      "jamity",
      "janedoe",
      "janesinner",
      "janette",
      "jef",
      "jessi",
      "jessi77",
      "jessicalou",
      "joker",
      "juliju",
      "july",
      "kacy",
      "kailah",
      "kala",
      "kaotika",
      "karanlit",
      "katherine",
      "kaylindear",
      "kemper",
      "khalida",
      "kirbee",
      "konekoya",
      "kush",
      "lady",
      "ladyviolet",
      "lait",
      "lalka",
      "lanei",
      "larabeard",
      "lascaux",
      "lass",
      "lavonne",
      "leella",
      "leelou",
      "leetattar",
      "lego",
      "lelly",
      "lerahon",
      "lestis",
      "lexiexj",
      "leza",
      "lia_cobain",
      "lillianrose",
      "lillibayle",
      "lilymai",
      "lilyt",
      "linziebelle",
      "liryc",
      "littlecalypso",
      "liu",
      "livay",
      "liya",
      "lizstein",
      "lockhart",
      "lolitta",
      "lollimila",
      "lovelace",
      "loveless",
      "luana",
      "lucerne",
      "lucid",
      "lufae",
      "lumo",
      "lunar",
      "lure",
      "luxlee",
      "lylie",
      "lyn",
      "lyuba",
      "lyvia",
      "lyxzen",
      "machete",
      "magpie",
      "mah",
      "malicatsz",
      "malika",
      "malvyna",
      "mandeelou",
      "manko",
      "maokiz",
      "marajade",
      "margarette",
      "mari",
      "mariajupiter",
      "marlene",
      "marquise",
      "mars",
      "maud",
      "meela",
      "mel",
      "melanya",
      "melbee",
      "mendacia",
      "mercury",
      "mermaid",
      "mialeveret",
      "miele",
      "milenci",
      "mille",
      "milloux",
      "mimzyy",
      "missfernandez",
      "mistica",
      "mistyy",
      "mnemozyne",
      "moon",
      "morena",
      "morikat",
      "morrigan",
      "nadeshda",
      "narciss",
      "nataliejanex",
      "natashalegeyda",
      "nattybohh",
      "nebula",
      "nefka",
      "nemesis",
      "nery",
      "ness",
      "nikkisleepy",
      "noel",
      "nubia",
      "nykki",
      "nymphony",
      "octaviamay",
      "okami",
      "ondinae",
      "oogie",
      "orion",
      "oro",
      "paigewinters",
      "paloma",
      "palomadias",
      "pandie",
      "pandub",
      "patton",
      "paulam",
      "pawlaraw",
      "pekopeko",
      "penny",
      "pesky",
      "phoenix",
      "pia",
      "pialora",
      "piedpiper",
      "pilot",
      "pirate",
      "plum",
      "polaw",
      "poox",
      "porcelinna",
      "porphyria",
      "posh",
      "priscila",
      "prism",
      "prussia",
      "psyco_",
      "pulp",
      "pvris",
      "pyke",
      "queenblossom",
      "rachelle",
      "radeo",
      "raleigh",
      "rambo",
      "raphaelite",
      "rawruh",
      "rebel_line",
      "rebyt",
      "redsnow",
      "reed",
      "refen",
      "revenge",
      "rhue",
      "riae",
      "ripley",
      "riversong",
      "rjen",
      "romaica",
      "rouge",
      "rourke",
      "sabbbre",
      "saint",
      "salliss",
      "saralilith",
      "saria",
      "sashabrink",
      "satelina",
      "satin",
      "satinkaa",
      "saturn",
      "savanna",
      "scribbles",
      "sedona",
      "sejmet",
      "selenite",
      "serenna",
      "shamandalie",
      "shannon_",
      "shaunanyx",
      "shay",
      "sheer",
      "shine",
      "shotgun",
      "sibila",
      "siderealbreeze",
      "siko",
      "silencia",
      "silvery",
      "silvi",
      "sinnah",
      "sinni",
      "sivir",
      "skinbyrd",
      "skydot",
      "skyhook",
      "slim",
      "smash",
      "smoky",
      "snowyfeles",
      "somer",
      "sophoulla",
      "soul",
      "spliff",
      "spock",
      "stark",
      "stephy",
      "stephyrodriguez",
      "sterling_",
      "stormyent",
      "sua",
      "sugarkane90",
      "sugarr",
      "sundew",
      "sunny",
      "tabbytha",
      "talena",
      "tangerine",
      "tanyabat",
      "tarion",
      "tasha",
      "tchip",
      "teenarancore",
      "tegnwar",
      "terrorlydia",
      "terrox",
      "teslaa",
      "thaiz",
      "thatty",
      "tigh",
      "tinyphantom",
      "titan_",
      "torasuicide",
      "totiga",
      "trebble",
      "trece",
      "tristyn",
      "twiitch",
      "ultima",
      "ultramarie",
      "undead",
      "uynn",
      "vaire",
      "valeryraccoon",
      "vandoll",
      "vaniislima",
      "vanp",
      "vareon",
      "vassilis",
      "vaughn",
      "vehera",
      "veive",
      "velour",
      "venera",
      "venom",
      "verde",
      "vice",
      "viking",
      "vikki",
      "viky",
      "vivid",
      "waikiki",
      "wednesdaya",
      "whiskey",
      "wichitak",
      "wilhelmine",
      "wingsweak",
      "wolf",
      "xuxa",
      "yegola",
      "yesenia",
      "your_mjsty",
      "yugen",
      "yuke",
      "yuno",
      "yupa",
      "yusuracircus",
      "yuxi",
      "zad",
      "zeezee",
      "zell",
      "zenit",
      "zephi",
      "zilvia",
      "zippo",
      "zoli"
    ).sorted.distinct

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

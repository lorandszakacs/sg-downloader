package home.sg.constants

object Version {
  val majorVersion = 0
  val minorVersion = 1

  val postFix = "";

  def version(): String = "v " + majorVersion + "." + minorVersion + postFix
}
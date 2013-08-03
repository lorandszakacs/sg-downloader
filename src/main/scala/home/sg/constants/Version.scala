package home.sg.constants

object Version {
  val MajorVersion = 0
  val MinorVersion = 1

  val PostFix = "";

  def version(): String = "v " + MajorVersion + "." + MinorVersion + PostFix
}
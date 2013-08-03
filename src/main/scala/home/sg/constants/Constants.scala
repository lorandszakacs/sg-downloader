package home.sg.constants

import com.typesafe.config.ConfigFactory

object Constants {
  private object PropertyKeys {
    val User = "sg-downloader.user"
    val DefaultDownloadPath = "sg-downloader.download-path"
    val DefaultUpdatePath = "sg-downloader.update-path"
    val DefaultInputFile = "sg-downloader.input-file"
  }

  private val conf = ConfigFactory.load()
  val UserName = conf.getString(PropertyKeys.User)
  val DefaultDownloadPath = conf.getString(PropertyKeys.DefaultDownloadPath)
  val DefaultUpdatePath = conf.getString(PropertyKeys.DefaultUpdatePath)
  val DefaultInputPath = conf.getString(PropertyKeys.DefaultInputFile)
}
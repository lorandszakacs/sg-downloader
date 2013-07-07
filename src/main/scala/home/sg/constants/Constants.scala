package home.sg.constants

import com.typesafe.config.ConfigFactory

object Constants {
  private object PropertyKeys {
    val user = "sg-downloader.user"
    val defaultDownloadPath = "sg-downloader.download-path"
    val defaultUpdatePath = "sg-downloader.update-path"
    val defaultInputFile = "sg-downloader.input-file"
  }

  private val conf = ConfigFactory.load()
  val userName = conf.getString(PropertyKeys.user)
  val defaultDownloadPath = conf.getString(PropertyKeys.defaultDownloadPath)
  val defaultUpdatePath = conf.getString(PropertyKeys.defaultUpdatePath)
  val defaultInputPath = conf.getString(PropertyKeys.defaultInputFile)
}
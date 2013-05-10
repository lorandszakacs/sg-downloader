package home.sg.client

abstract class SGException(msg: String) extends Exception(msg)

case class LoginLostException(val msg: String) extends SGException(msg)

case class DownloadingMRSetException(val msg: String) extends SGException(msg)

case class UnknownSGException(val msg: String) extends SGException(msg)

case class HttpClientException(val msg: String) extends SGException(msg)

case class FileDownloadException(val msg: String) extends SGException(msg)

case class InexistentFileException(val msg: String, val fileURL: String) extends SGException(msg)
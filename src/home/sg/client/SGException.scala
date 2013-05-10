package home.sg.client

abstract class SGException(msg: String) extends Exception(msg)

case class LoginInvalidUserOrPasswordExn(val msg: String) extends SGException(msg)

case class LoginConnectionLostException(val msg: String) extends SGException(msg)

case class LoginUnknownException(val msg: String) extends SGException(msg)

case class HttpClientException(val msg: String) extends SGException(msg)

case class FileDownloadException(val msg: String) extends SGException(msg)
case class FileWrittingException(val msg: String) extends SGException(msg)

case class UnknownSGException(val msg: String) extends SGException(msg)

//these two have no dire consequences and are used for signaling, mostly
case class InexistentFileException(val msg: String, val fileURL: String) extends SGException(msg)
case class DownloadingMRSetException(val msg: String) extends SGException(msg)


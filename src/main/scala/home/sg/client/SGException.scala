package home.sg.client

sealed abstract class SGException(msg: String) extends Exception

case class LoginInvalidUserOrPasswordExn(val msg: String) extends SGException(msg)

case class LoginConnectionLostException(val msg: String) extends SGException(msg)

case class LoginUnknownException(val msg: String) extends SGException(msg)

case class HttpClientException(val msg: String) extends SGException(msg)

case class FileDownloadException(val msg: String) extends SGException(msg)

case class FileWrittingException(val msg: String) extends SGException(msg)

case class UnknownSGException(val msg: String) extends SGException(msg)
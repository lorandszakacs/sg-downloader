package com.lorandszakacs.sgd.http

import spray.http.Uri

trait Client {

  def initialAccessPoint: Uri
  def loginAccessPoint: Uri

  def userName: String
  def password: String

  def authentication: AuthenticationInfo

}
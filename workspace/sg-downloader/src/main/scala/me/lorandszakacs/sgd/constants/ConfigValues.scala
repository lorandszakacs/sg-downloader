/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Lorand Szakacs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.lorandszakacs.sgd.constants

import com.typesafe.config.ConfigFactory

object ConfigValues {
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
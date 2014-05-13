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
package com.lorandszakacs.util.html

import java.io.File
import java.security.InvalidParameterException

import org.jsoup.Jsoup

object HtmlProcessor {
  def apply(contents: String) = {
    new HtmlProcessor(Some(contents), None)
  }

  def apply(file: File) = {
    new HtmlProcessor(None, Some(file))
  }
}

class HtmlProcessor private (content: Option[String], file: Option[File]) {
  private lazy val document = {
    (content, file) match {
      case (None, Some(f)) => Jsoup.parse(f, "UTF-8")
      case (Some(c), None) => Jsoup.parse(c)
      case (Some(_), Some(_)) => throw new InvalidParameterException("Cannot instantiate an HtmlProcessor with both string content and a file")
      case (None, None) => throw new InvalidParameterException("Cannot instantiate an HtmlProcessor with nothing")
    }
  }

  def filter(f: HtmlFilter): Option[List[String]] = {
    f.apply(document)
  }
}
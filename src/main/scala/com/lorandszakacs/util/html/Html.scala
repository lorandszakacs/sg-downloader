/**
 * Copyright 2015 Lorand Szakacs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.lorandszakacs.util.html

import java.io.File

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
object Html {
  def apply(contents: String) = new Html(Jsoup.parse(contents))

  def apply(file: File) = new Html(Jsoup.parse(file, "UTF-8"))
}

class Html private(val document: Document) {
  def filter(f: HtmlFilter): Option[List[String]] = f.apply(document)
}
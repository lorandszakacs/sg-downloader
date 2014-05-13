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
package com.lorandszakacs.sgd.parser.html

import com.lorandszakacs.util.http.SGClient

object PhotoAlbumBuilder {

  /**
   * @param client; an already logged in client
   *
   * @return
   */
  def buildSetHeaders(sgName: String, client: SGClient): List[PhotoSetHeader] = {
    val albumPage = client.getAlbumPage(sgName)
    val setHeaders = SGPageParser.parseSetAlbumPageToSetHeaders(sgName, albumPage)
    setHeaders
  }

  def buildPhotoSet(header: PhotoSetHeader, client: SGClient, report: (Any => Unit)): PhotoSet = {
    report("fetching page info: %s".format(header.toString))
    val setPage = client.getPage(header.URL)
    val imageURLs = SGPageParser.parseSetPageToImageURLs(setPage)
    new PhotoSet(header, imageURLs)
  }

}
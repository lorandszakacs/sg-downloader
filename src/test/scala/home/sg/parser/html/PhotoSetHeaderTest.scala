/**
 * Copyright (c) 2013 Lorand Szakacs
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
package home.sg.parser.html

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PhotoSetHeaderTest extends FunSuite {

  private abstract class ExpectedValues(val initialData: (String, String, String, String), val date: String, val title: String, val URL: String, val saveLocation: String)

  private val sashMRSetRaw = (
    "Sash",
    "<div class=\"preview\" id=\"album_277610\" title=\"Sash: The Grove\">",
    "<a class=\"pngSpank\" href=\"/members/Sash/albums/site/33360/\"/><img src=\"/media/albums/0/36/33360/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls: The Grove\" title=\"Sash: The Grove\" /></a>",
    "<div class=\"date\">Apr 10, 2013</div>")

  private object sashMRSet extends ExpectedValues(
    sashMRSetRaw,
    "2013.04",
    "The Grove",
    "http://suicidegirls.com/members/Sash/albums/site/33360/",
    "Sash/2013.04 - The Grove")

  private val sashMRSetWithDuplicateSpacesInNameRaw = (
    "Sash",
    "<div class=\"preview\" id=\"album_277610\" title=\"Sash: The  Grove\">",
    "<a class=\"pngSpank\" href=\"/members/Sash/albums/site/33360/\"/><img src=\"/media/albums/0/36/33360/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls: The Grove\" title=\"Sash: The Grove\" /></a>",
    "<div class=\"date\">Apr 10, 2013</div>")

  private object sashMRSetWithDuplicateSpacesInName extends ExpectedValues(
    sashMRSetWithDuplicateSpacesInNameRaw,
    "2013.04",
    "The Grove",
    "http://suicidegirls.com/members/Sash/albums/site/33360/",
    "Sash/2013.04 - The Grove")

  private val sashPinkSetRaw = (
    "Sash",
    "<div class=\"preview\" id=\"album_273231\" title=\"Sash: Arboraceous\">",
    " <a class=\"pngSpank\" href=\"/girls/Sash/photos/Arboraceous/\"><img src=\"/media/girls/Sash/photos/Arboraceous/setpreview_medium.jpg\" ... /></a>",
    "<div class=\"date\">Mar 6, 2013</div>")

  private object sashPinkSet extends ExpectedValues(
    sashPinkSetRaw,
    "2013.03",
    "Arboraceous",
    "http://suicidegirls.com/girls/Sash/photos/Arboraceous/",
    "Sash/2013.03 - Arboraceous")

  private val nahpPinkSetWithSpacesRaw = (
    "Nahp",
    "<div class=\"preview\" id=\"album_271379\" title=\"Nahp:   Girl Next Door\">",
    "<a class=\"pngSpank\" href=\"/girls/Nahp/photos/++Girl+Next+Door/\"><img src=\"/media/girls/Nahp/photos/  Girl Next Door/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls:   Girl Next Door\" /></a>",
    "<div class=\"date\">Jan 1, 2013</div>")

  private object nahpPinkSetWithSpaces extends ExpectedValues(
    nahpPinkSetWithSpacesRaw,
    "2013.01",
    "Girl Next Door",
    "http://suicidegirls.com/girls/Nahp/photos/++Girl+Next+Door/",
    "Nahp/2013.01 - Girl Next Door")

  //TODO: refactor this to support Hopeful sets naturally
  private val dalmascaHopefulSetRaw = (
    "Dalmasca",
    "<h1><a href=\"/members/Dalmasca/albums/site/33209/\">Picker-Uppers</a></h1>",
    "<span class=\"prefix\" title=\"This set is in the Member Review section. Members can see and comment on it.\">UP SINCE:</span> Mar 23 2013				</p>",
    "")

  private object dalmascaHopefulSet extends ExpectedValues(
    dalmascaHopefulSetRaw,
    "2013.03",
    "Picker-Uppers",
    "http://suicidegirls.com/members/Dalmasca/albums/site/33209/",
    "Dalmasca/2013.03 - Picker-Uppers")

  private def assertCorrectValues(expected: ExpectedValues) = {
    val sgName = expected.initialData._1
    val preview = expected.initialData._2
    val pngSpank = expected.initialData._3
    val date = expected.initialData._4

    val result = PhotoSetHeader(sgName, preview, pngSpank, date);
    assert(result.sgName === sgName, "SG name mismatch: expected \"%s\"".format(sgName))
    assert(result.title === expected.title, "title mismatch: expected %s".format(expected.title))
    assert(result.URL === expected.URL, "URL mismatch: expected %s".format(expected.URL))
    assert(result.date === expected.date, "date mismatch: expected %s".format(expected.date))
    result
  }

  private def assertCorrectValuesHopeful(expected: ExpectedValues) = {
    val sgName = expected.initialData._1
    val header = expected.initialData._2
    val date = expected.initialData._3

    val result = PhotoSetHeader(sgName, header, date);
    assert(result.sgName === sgName, "SG name mismatch: expected %s".format(sgName))
    assert(result.title === expected.title, "title mismatch: expected \"%s\"".format(expected.title))
    assert(result.URL === expected.URL, "URL mismatch: expected %s".format(expected.URL))
    assert(result.date === expected.date, "date mismatch: expected %s".format(expected.date))
    result
  }

  test("MR set, Sash, The Grove") {
    assertCorrectValues(sashMRSet)
  }
  
  test("MR set, Sash, The Grove with duplicate spaces") {
    assertCorrectValues(sashMRSetWithDuplicateSpacesInName)
  }
  

  test("pink set, Sash, Arboraceous") {
    assertCorrectValues(sashPinkSet)
  }

  test("pink set with spaces in title, Nahp, Girl Next Door") {
    assertCorrectValues(nahpPinkSetWithSpaces)
  }

  test("hopeful set, Dalmasca - Picker-Uppers") {
    assertCorrectValuesHopeful(dalmascaHopefulSet)
  }

}
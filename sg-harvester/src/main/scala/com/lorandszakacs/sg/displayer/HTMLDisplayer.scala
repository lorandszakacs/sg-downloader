package com.lorandszakacs.sg.displayer

import java.net.URL

import com.lorandszakacs.sg.model._

/**
  * Created by lorand on 7/11/16.
  */
object HTMLDisplayer {

  def modelIndex(indexFileName: String)(ms: List[ModelDisplay]): String = {
    def itemLink(m: ModelDisplay): String = {
      s"""|<li><a href="${m.name.name}/$indexFileName">${m.name.name}</li>
          |""".stripMargin
    }
    s"""
       |<!DOCTYPE html>
       |<html>
       |  <title>Suicide Girls</title>
       |  <ol type="1">
       |    ${ms.sortBy(_.name).map(itemLink).mkString("\n")}
       |  </ol>
       |</html>
    """.stripMargin
  }

  def modelToHTML(m: Model): ModelDisplay = {
    val displays = m.photoSets map { ps =>
      PhotoSetDisplay(
        name = s"${m.name.name}_${ps.date}_${ps.title.name.replace(" ", "_")}.html",
        html = photoSetHtml(m)(ps)
      )
    }
    ModelDisplay(
      name = m.name,
      photoSets = displays,
      photoSetIndex(m)(displays)
    )
  }


  private def photoSetIndex(m: Model)(displays: List[PhotoSetDisplay]): String = {
    def itemLink(d: PhotoSetDisplay): String = {
      s"""|<li><a href="${d.name}">${d.name}</li>
          |""".stripMargin
    }
    s"""
       |<!DOCTYPE html>
       |<html>
       |<title>${m.name.externalForm}</title>
       |  <h2><a href="../index.html">BACK</a></h2>
       |  <ol type="1">
       |    ${displays.map(itemLink).mkString("\n")}
       |  </ol>
       |</html>
    """.stripMargin
  }

  private def photoSetHtml(m: Model)(s: PhotoSet): String = {
    def divId(photo: Photo): String = s"img${photo.index}"
    def firstContainer(photo: Photo): String = {
      s"""
         |         <div class="w3-col s3 w3-container">
         |            <a class="w3-hover-opacity">
         |            <img src="${photo.url.toExternalForm}" alt="${divId(photo)}" style="width:100%">
         |            </a>
         |         </div>
      """.stripMargin
    }

    s"""
       |<!DOCTYPE html>
       |<html>
       |   <title>${m.name.externalForm}: ${s.title.externalForm}</title>
       |   <meta name="viewport" content="width=device-width, initial-scale=1">
       |   <link rel="stylesheet" href="https://dl.dropboxusercontent.com/u/11532620/suicide-girls/css/w3.css">
       |   <style>
       |      .picture {display:none}
       |   </style>
       |   <body>
       |      <div class="w3-container">
       |         <h2>${m.name.externalForm}: ${s.title.externalForm} - ${s.date}</h2>
       |         <h2><a href="../${m.name.name}/index.html">BACK</a></h2>
       |      </div>
       |
       |      <div class="w3-row">
       |      ${s.photos.map(phs => firstContainer(phs)).mkString("\n")}
       |      </div>
       |   </body>
       |</html>
    """.stripMargin
  }

}

case class ModelDisplay(
  name: ModelName,
  photoSets: Seq[PhotoSetDisplay],
  photoSetIndexHtml: String
)

case class PhotoSetDisplay(
  name: String,
  html: String
)
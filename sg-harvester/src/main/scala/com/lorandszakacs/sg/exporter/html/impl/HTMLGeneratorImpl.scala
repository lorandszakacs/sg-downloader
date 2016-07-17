package com.lorandszakacs.sg.exporter.html.impl

import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.monads.future.FutureUtil._

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[html] class HTMLGeneratorImpl()(
  implicit val ec: ExecutionContext
) extends HTMLGenerator {

  override def createHTMLPageForModels(models: List[Model])(implicit settings: HtmlSettings): Future[ModelsRootIndex] = {
    for {
      modelIndexes: List[ModelIndex] <- Future.traverse(models) { model => Future(modelIndex(model)) }
    } yield {
      ModelsRootIndex(
        indexFileName = settings.indexFileName,
        html = rootIndexPage(modelIndexes),
        models = modelIndexes
      )
    }
  }

  private def modelIndex(m: Model)(implicit settings: HtmlSettings): ModelIndex = {
    def photoSetIndex(m: Model)(psi: List[PhotoSetIndex])(implicit settings: HtmlSettings): Html = {
      def photoSetLink(d: PhotoSetIndex): String = {
        s"""|<li><a href="${d.htmlFileName}">${d.displayName}</a></li>
            |""".stripMargin
      }
      Html(
        s"""
           |<!DOCTYPE html>
           |<html>
           |<title>${m.name.externalForm}</title>
           |  <h2><a href="../${settings.indexFileName}">BACK</a></h2>
           |  <h2>${m.stringifyType.capitalize}: ${m.name.externalForm}</h2>
           |  <ol type="1">
           |    ${psi.map(photoSetLink).mkString("\n")}
           |  </ol>
           |</html>
    """.stripMargin
      )
    }
    def photoSetPage(m: Model)(ps: PhotoSet)(implicit settings: HtmlSettings): PhotoSetIndex = {
      def photoDiv(photo: Photo): String = {
        s"""|<div class="w3-col s3 w3-container">
            |   <a class="w3-hover-opacity">
            |     <img src="${photo.url.toExternalForm}" alt="${photo.index}" style="width:100%">
            |   </a>
            |</div>""".stripMargin
      }

      val html = Html(
        s"""
           |<!DOCTYPE html>
           |<html>
           |   <title>${m.name.externalForm}: ${ps.title.externalForm}</title>
           |   <meta name="viewport" content="width=device-width, initial-scale=1">
           |   <link rel="stylesheet" href="https://dl.dropboxusercontent.com/u/11532620/suicide-girls/css/w3.css">
           |   <style>
           |      .picture {display:none}
           |   </style>
           |   <body>
           |      <div class="w3-container">
           |         <h2>${m.name.externalForm}: ${ps.title.externalForm} - ${ps.date}</h2>
           |         <h2><a href="../${m.name.name}/${settings.indexFileName}">BACK</a></h2>
           |      </div>
           |
           |      <div class="w3-row">
           |${ps.photos.map(phs => photoDiv(phs)).mkString("\n")}
           |      </div>
           |   </body>
           |</html>
    """.stripMargin
      )

      val htmlName = s"${m.name.externalForm}_${ps.date}_${ps.title.name}.html"
      PhotoSetIndex(
        html = html,
        htmlFileName = htmlName.replaceAll("[^a-zA-Z0-9.-]", "_"),
        displayName = s"${ps.date}: ${ps.title.name}"
      )
    }

    val photoSets: List[PhotoSetIndex] = m.photoSets map photoSetPage(m)
    val modelIndexHtml = photoSetIndex(m)(photoSets)
    ModelIndex(
      name = m.name,
      modelIndexHtml = modelIndexHtml,
      modelIndexHtmlFileName = s"${m.name.name}/${settings.indexFileName}",
      photoSets = photoSets
    )
  }

  private def rootIndexPage(models: List[ModelIndex])(implicit settings: HtmlSettings): Html = {
    def itemLink(m: ModelIndex): String = {
      s"""<li><a href="${m.modelIndexHtmlFileName}">${m.name.name}</a></li>""".stripMargin
    }
    Html(
      s"""
         |<!DOCTYPE html>
         |<html>
         |  <title>${settings.rootIndexTitle}</title>
         |  <ol type="1">
         |${models.sortBy(_.name).map(itemLink).mkString("\t\t", "\n\t\t", "\n")}
         |  </ol>
         |</html>
      """.stripMargin
    )
  }


}

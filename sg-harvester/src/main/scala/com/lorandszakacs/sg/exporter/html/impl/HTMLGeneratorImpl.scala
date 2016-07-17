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
        html = rootIndexPage(modelIndexes),
        models = modelIndexes
      )
    }
  }

  override def createRootIndex(models: List[ModelName])(implicit settings: HtmlSettings): Future[Html] = {
    Future.successful(rootIndexPageForModelNames(models))
  }

  private def modelIndex(m: Model)(implicit settings: HtmlSettings): ModelIndex = {
    def modelIndexHtmlPage(m: Model)(psi: List[PhotoSetIndex])(implicit settings: HtmlSettings): Html = {
      def photoSetLink(photoSet: PhotoSetIndex): String = {
        s"""|<li><a href="${photoSet.html.relativePathAndName}">${photoSet.displayName}</a></li>
            |""".stripMargin
      }
      Html(
        relativePathAndName = modelIndexPageForName(m.name),
        value =
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

    def photoSetIndexPage(m: Model)(ps: PhotoSet)(implicit settings: HtmlSettings): PhotoSetIndex = {
      def photoDiv(photo: Photo): String = {
        s"""|<div class="w3-col s3 w3-container">
            |   <a class="w3-hover-opacity">
            |     <img src="${photo.url.toExternalForm}" alt="${photo.index}" style="width:100%">
            |   </a>
            |</div>""".stripMargin
      }

      val html = Html(
        relativePathAndName = s"${modelIndexPageForName(m.name)}/${m.name.externalForm}_${ps.date}_${ps.title.name}.html".replaceAll("[^a-zA-Z0-9.-]", "_"),
        value =
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
             |         <h2><a href="../${modelIndexPageForName(m.name)}">BACK</a></h2>
             |      </div>
             |
           |      <div class="w3-row">
             |${ps.photos.map(phs => photoDiv(phs)).mkString("\n")}
             |      </div>
             |   </body>
             |</html>
    """.stripMargin
      )

      PhotoSetIndex(
        html = html,
        displayName = s"${ps.date}: ${ps.title.name}"
      )
    }

    val photoSets: List[PhotoSetIndex] = m.photoSets map photoSetIndexPage(m)
    val modelIndexHtml = modelIndexHtmlPage(m)(photoSets)
    ModelIndex(
      name = m.name,
      modelIndexHtml = modelIndexHtml,
      photoSets = photoSets
    )
  }

  private def rootIndexPage(models: List[ModelIndex])(implicit settings: HtmlSettings): Html = {
    generateRootIndexPage(models)(
      title = settings.rootIndexTitle,
      linkAndItemNameGenerator = { m: ModelIndex =>
        (m.modelIndexHtml.relativePathAndName, m.name.name)
      }
    )
  }

  private def rootIndexPageForModelNames(modelNames: List[ModelName])(implicit settings: HtmlSettings): Html = {
    generateRootIndexPage(modelNames)(
      title = settings.rootIndexTitle,
      linkAndItemNameGenerator = { m: ModelName =>
        (modelIndexPageForName(m), m.name)
      }
    )
  }

  private def generateRootIndexPage[T](els: List[T])(title: String, linkAndItemNameGenerator: T => (String, String))(implicit settings: HtmlSettings): Html = {
    def item(el: T) = {
      val (link, name) = linkAndItemNameGenerator(el)
      s"""<li><a href="$link">$name</a></li>"""
    }
    Html(
      relativePathAndName = settings.indexFileName,
      value =
        s"""
           |<!DOCTYPE html>
           |<html>
           |  <title>$title</title>
           |  <ol type="1">
           |${els.map(item).mkString("\t\t", "\n\t\t", "\n")}
           |  </ol>
           |</html>
      """.stripMargin
    )
  }

  private def modelIndexPageForName(m: ModelName)(implicit settings: HtmlSettings): String = {
    s"${m.name}/${settings.indexFileName}"
  }


}

util-html-filter
================

This is a very simple, easy to use library that can be used to extract information from html code by combining different pre-defined filters. Filters are combined  using a very intuitive syntax.  

## Running it

* can be easily built using [sbt](http://www.scala-sbt.org/)
* depends on the jsoup library; if you build it with sbt then sbt will take care of downloading it for you.  
  * sbt is configured to use the eclipse plugin. Once you start sbt you can run the `eclipse` task and it will create a ready-to-import [ScalaIDE](http://scala-ide.org/) project.  
* if you wish to integrate this library into a larger project, consider using the [aggregate sbt project template](https://github.com/lorandszakacs/sbt-project-templates/tree/master/aggregate-project-template) I wrote.

## Using it

First, check out the [tests](blob/master/src/test/scala/com/lorandszakacs/util/html/HtmlProcessorTest.scala) for usage examples.  


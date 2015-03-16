util-html
================

This is a very simple, easy to use library that can be used to extract information from html code by combining different pre-defined filters. Filters are combined  using a very intuitive syntax.  

## Current version

'0.1.1'

## Installing it

* can be easily built using [sbt](http://www.scala-sbt.org/)
* add '"com.lorandszakacs" %% "util-html" % "0.1.1" withSources()' to your build definition.

## Using it

First, check out the [tests](src/test/scala/com/lorandszakacs/util/html/HtmlProcessorTest.scala) for usage examples.  

### Examples

Consider the following chunk of html code:  
```html
<div class="importantInfo">
    <a href="http://www.metallica.com/">Metallica \m/ </a>
</div>
<div class="importantInfo">
    <a href="http://www.blueoystercult.com/">BoC \m/ </a>
</div>


<div class="unimportantInfo">
    <a href="http://www.nickelback.com/"> Whatever</a>
</div>
```

Let's see how we use the library:
```scala
	val html = HtmlProcessor(/*a file, or string containing the above html code*/)
	
	//this line will retain all the div that have the class "importantInfo"
	val result = html filter Class("importantInfo")
	println(result)
```  
The above code will yield the result:
```
List(
<div class="importantInfo">
  <a href="http://www.metallica.com/">Metallica \m/ </a>
</div>,
<div class="importantInfo">
 <a href="http://www.blueoystercult.com/">BoC \m/ </a>
</div>)
```
It gives us the two html elements that have the class "importantInfo".  

Want to get hold only of the links within these two divs? Here's how:
```scala
html filter Class("importantInfo") && HrefLink()

//result:
List(http://www.metallica.com/, http://www.blueoystercult.com/)
```

What about if we want to grab only the content from `<a>` tags within the two divs?
```scala
html filter Content(Class("importantInfo") && Tag("a"))

//result:
List(Metallica \m/, BoC \m/)
```

You can also grab the values of attributes, actually the `HrefLink` filter is nothing but a special case of this:
```scala
html filter Class("importantInfo") && Tag("a") && Value(Attribute("href"))

//result:
List(http://www.metallica.com/, http://www.blueoystercult.com/)
```

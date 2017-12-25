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
package com.lorandszakacs.util.html.data

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object SimplifiedData {

  object FilterClass {

    val ClassWithSpaceInTheName =
      """
<div id="login">
  <a class="button login">Login</a>
  <div id="login-wrapper">
    <div id="login-bg"></div>
    <div class="login-form-wrapper">
      <form id="login-form" method="post" action="/login/"><input type='hidden' name='csrfmiddlewaretoken' value='7y4WqXpM01vHwaqTYACLmcEa43b1FyFu' />
        <div>
          <div><!-- we built div city! -->
            <div class="errors"></div>
            <input id="username" autocapitalize="off" name="username" maxlength="254" />
            <input type="password" name="password" maxlength="100" />
            <button type="submit" class="button call-to-action">Login</button>
            <a class="forgot-password" href="/help/">Forgot Password?</a>
          </div>
        </div>
      </form>
    </div>
  </div>
</div>
"""

    val FlatClasses =
      """
<div class="meta-data">bogus1</div>

<div class="meta-data">bogus2</div>
"""

    val NestedClasses =
      """
<div class="meta-data">
	bogus
	<div class="meta-data">bogus</div>
</div>
"""

    val SingleClass =
      """
<div class="meta-data">
	bogus
</div>
"""
  }

  object FilterContent {

    val ContentFromAttribute =
      """
<a id="load-more" data-load-more-method="offset">Load more</a>
"""

    val ContentFromClass =
      """
<div class="meta-data">
	<time class="icon-photography"> Nov 09, 2013 </time>
	<span class="photographer">
	 by
	  <a href="link-to-photographer"></a>
	</span>
</div>
"""

    val ContentFromComposite =
      """
<div class="meta-data">
	<time class="icon-photography"> Nov 09, 2013 </time>
	<span class="photographer">
	 by
	  <a href="link-to-photographer"></a>
	</span>
</div>
"""

    val ContentFromTag =
      """
<div>
	<a>whatever</a>
</div>
"""
  }

  object FilterLink {

    val FlatLinks =
      """
<a href="first-link/foo">
	<figure data-width="" data-height="" data-ratio=""
		class="ratio-16-9 res-image">
		<noscript data-tablet="" data-mobile="" data-retina="junk.jpg"
			data-src="junk2.jpg">
			<img src="junk3.jpg" class="ratio-16-9" alt="" />
		</noscript>
	</figure>
</a>
<a href="second-link/foo"> </a>
"""

    val NestedLinks =
      """
<a href="first-link/foo">
	<figure data-width="" data-height="" data-ratio=""
		class="ratio-16-9 res-image">
		<noscript data-tablet="" data-mobile="" data-retina="junk.jpg"
			data-src="junk2.jpg">
			<img src="junk3.jpg" class="ratio-16-9" alt="" />
		</noscript>
	</figure> <a href="second-link/foo"> </a>
</a>
"""

    val SingleLink =
      """
<a href="first-link/foo">
	<figure data-width="" data-height="" data-ratio=""
		class="ratio-16-9 res-image">
		<noscript data-tablet="" data-mobile="" data-retina="junk.jpg"
			data-src="junk2.jpg">
			<img src="junk3.jpg" class="ratio-16-9" alt="" />
		</noscript>
	</figure>
</a>
"""
  }

  object FilterTag {

    val FlatTags =
      """
<a href="first-link/foo">
	<figure data-width="" data-height="" data-ratio=""
		class="ratio-16-9 res-image">
		<noscript data-tablet="" data-mobile="" data-retina="junk.jpg"
			data-src="junk2.jpg">
			<img src="junk3.jpg" class="ratio-16-9" alt="" />
		</noscript>
	</figure>
</a>
<a href="second-link/foo"> </a>
"""

    val NestedTagsWithinFlatTags =
      """
<a href="first-link/foo">
	 <a href="second-link/foo"> 
	 	<a href="third-link/foo">
	 	 </a>
	 </a>
</a>
<a href="second/first-link/foo">
	 <a href="second/second-link/foo"> 
	 	<a href="second/third-link/foo">
	 	 </a>
	 </a>
</a>
"""

    val NestedTags =
      """
<a href="first-link/foo">
	 <a href="second-link/foo"> 
	 	<a href="third-link/foo">
	 	 </a>
	 </a>
</a>
"""

    val SingleTag =
      """
<a href="first-link/foo"></a>
"""
  }
}

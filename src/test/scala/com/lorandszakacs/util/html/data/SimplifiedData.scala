package com.lorandszakacs.util.html.data

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
    val FlatLinks = """"""
    val NestedLinks = """"""
    val SingleLink = """"""
  }

  object FilterTag {
    val FlatTags = """"""
    val NestedTagsWithinFlatTags = """"""
    val NestedTafs = """"""
  }
}
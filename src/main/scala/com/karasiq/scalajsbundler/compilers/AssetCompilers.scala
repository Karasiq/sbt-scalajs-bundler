package com.karasiq.scalajsbundler.compilers

case class AssetCompilers(pf: PartialFunction[String, AssetCompiler]) extends AnyVal {
  def apply(mime: String): AssetCompiler = {
    pf.applyOrElse(mime, (_: String) ⇒ ConcatCompiler)
  }
}


object AssetCompilers {
  def default: AssetCompilers = AssetCompilers {
    case "text/javascript" ⇒
      JsClosureCompiler

    case "text/css" ⇒
      CssYuiCompiler

    case "text/html" ⇒
      HtmlCompiler
  }
}
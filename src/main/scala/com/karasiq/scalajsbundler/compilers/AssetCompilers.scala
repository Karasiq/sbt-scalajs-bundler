package com.karasiq.scalajsbundler.compilers

import scala.util.Try

case class AssetCompilers(pf: PartialFunction[String, AssetCompiler]) extends AnyVal {
  def apply(mime: String): AssetCompiler =
    pf.applyOrElse(mime, (_: String) => throw new IllegalArgumentException(s"No compiler defined for $mime"))

  def orElse(pf: PartialFunction[String, AssetCompiler]): AssetCompilers =
    AssetCompilers(this.pf.orElse(pf))

  def or(ac: AssetCompilers): AssetCompilers =
    this.orElse(ac.pf)

  @deprecated("Use .or(...)", "1.3.0")
  def <<=(ac: AssetCompilers): AssetCompilers =
    this.orElse(ac.pf)

  def orDefault: AssetCompilers =
    this.or(AssetCompilers.default)
}

object AssetCompilers {
  val default: AssetCompilers =
    AssetCompilers {
      case Mimes.coffeescript => CoffeeScriptCompiler

      case Mimes.less if classExists("com.github.sommeri.less4j.core.DefaultLessCompiler") =>
        newCompiler("Less4jCompiler")

      case Mimes.javascript if classExists("com.google.javascript.jscomp.Compiler") => newCompiler("JsClosureCompiler")

      case Mimes.javascript if classExists("com.yahoo.platform.yui.compressor.JavaScriptCompressor") =>
        newCompiler("JsYuiCompiler")

      case Mimes.css if classExists("com.yahoo.platform.yui.compressor.CssCompressor") => newCompiler("CssYuiCompiler")

      case Mimes.html if classExists("com.googlecode.htmlcompressor.compressor.HtmlCompressor") =>
        newCompiler("HtmlGoogleCompiler")

      case Mimes.jade if classExists("de.neuland.jade4j.Jade4J") => newCompiler("Jade4jCompiler")
    }

  def keepJavaScriptAsIs: AssetCompilers =
    AssetCompilers {
      case Mimes.javascript => ConcatCompiler
    }.or(default)

  def noMinimize: AssetCompilers =
    keepJavaScriptAsIs.orElse {
      case Mimes.css  => ConcatCompiler
      case Mimes.html => HtmlConcatCompiler
    }

  private[this] object Mimes extends PredefinedMimeTypes

  private[this] def classExists(className: String): Boolean =
    Try(Class.forName(className, false, getClass.getClassLoader)).isSuccess

  private[this] def newCompiler(className: String): AssetCompiler =
    Class.forName(s"com.karasiq.scalajsbundler.compilers.$className")
      .newInstance().asInstanceOf[AssetCompiler]
}

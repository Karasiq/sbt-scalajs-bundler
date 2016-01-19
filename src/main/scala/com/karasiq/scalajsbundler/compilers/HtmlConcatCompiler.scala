package com.karasiq.scalajsbundler.compilers

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent
import org.jsoup.Jsoup

object HtmlConcatCompiler extends AssetCompiler {
  def concat(htmlList: Seq[String]): String = {
    val parsed = htmlList.map { h ⇒
      val html = Jsoup.parse(h)
      html.head().html() → html.body().html()
    }

    """<!DOCTYPE html><html>""" +
      parsed.map(_._1).mkString("<head>", "\n", "</head>") +
      parsed.map(_._2).mkString("<body>", "\n", "</body>") +
      "</html>"
  }

  override def compile(contents: Seq[PageTypedContent]): String = {
    concat(contents.map(_.asset.asString))
  }
}

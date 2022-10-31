package com.karasiq.scalajsbundler.compilers

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConversions._

object HtmlConcatCompiler extends AssetCompiler {
  private implicit class ElementOps(val e: Element) extends AnyVal {
    def concatWith(src: Element): Unit = {
      @inline
      def delimit(delimiter: String, s1: String, s2: String): String =
        if (s1.endsWith(delimiter))
          s1 + s2
        else
          s1 + delimiter + s2

      src.attributes().foreach {
        case a if a.getKey == "class" => e.attr(a.getKey, delimit(" ", e.attr(a.getKey), a.getValue))

        case a if a.getKey == "style" => e.attr(a.getKey, delimit(";", e.attr(a.getKey), a.getValue))

        case a => // Replaces attribute value
          e.attr(a.getKey, a.getValue)
      }
      e.append(src.html())
    }
  }

  def concat(htmlList: Seq[String]): String = {
    val result = Jsoup.parse(htmlList.head)
    htmlList.tail.foreach { h =>
      val html = Jsoup.parse(h)
      result.head().concatWith(html.head())
      result.body().concatWith(html.body())
    }
    result.outerHtml()
  }

  override def compile(contents: Seq[PageTypedContent]): String =
    concat(contents.map(_.asset.asString))
}

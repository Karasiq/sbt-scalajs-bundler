package com.karasiq.scalajsbundler

import java.io._
import java.util.UUID

import com.karasiq.scalajsbundler.ScalaJSBundler._
import com.karasiq.scalajsbundler.compilers.{AssetCompilers, ConcatCompiler, HtmlConcatCompiler}
import com.karasiq.scalajsbundler.dsl._
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup

import scala.util.control.Exception

class ScalaJSBundleCompiler {
  private def writeAsset(asset: Asset, file: File): Unit = {
    require(file.getParentFile.isDirectory || file.getParentFile.mkdirs(), s"Not a directory: ${file.getParentFile}")
    val reader       = asset.content()
    val outputStream = new FileOutputStream(file)
    Exception.allCatch.andFinally { IOUtils.closeQuietly(outputStream); IOUtils.closeQuietly(reader) } {
      IOUtils.copyLarge(reader, outputStream)
    }
  }

  private def makeUrl(file: File): String =
    file.toString
      .split("[\\\\/]")
      .filter(_.nonEmpty)
      .mkString("/", "/", "")

  private def makeHtml(compilers: AssetCompilers, pages: Seq[PageHtml], assetsHtml: String): PageHtml = {
    def withAssets(html: String): String = {
      val page = Jsoup.parse(html)
      page.head().append(assetsHtml)
      page.outerHtml()
    }

    Html from compilers("text/html").compile(Seq(Html from withAssets(HtmlConcatCompiler.concat(pages.map {
      case PageHtml(asset, _, "text/html") => asset.asString

      case s @ PageHtml(_, _, mime) => compilers(mime).compile(Seq(s))
    }))))
  }

  private def makeScript(compilers: AssetCompilers, scripts: Seq[PageScript]): PageScript =
    Script from compilers("text/javascript").compile(Seq(Script from ConcatCompiler.concat(scripts.map {
      case PageScript(asset, _, "text/javascript") => asset.asString

      case s @ PageScript(_, _, mime) => compilers(mime).compile(Seq(s))
    })))

  private def makeStyle(compilers: AssetCompilers, styles: Seq[PageStyle]): PageStyle =
    Style from compilers("text/css").compile(Seq(Style from ConcatCompiler.concat(styles.map {
      case PageStyle(asset, _, "text/css") => asset.asString

      case s @ PageStyle(_, _, mime) => compilers(mime).compile(Seq(s))
    })))

  def createHtml(
      compilers: AssetCompilers,
      outputDir: String,
      name: String,
      contents: Seq[PageContent],
      inline: Boolean
    ): Unit = {
    require(new File(outputDir).isDirectory || new File(outputDir).mkdirs(), s"Not a directory: $outputDir")
    val script     = makeScript(compilers, contents.collect { case s: PageScript => s })
    val style      = makeStyle(compilers, contents.collect { case s: PageStyle => s })
    val static     = contents.collect { case f: PageFile => f }
    val assetsHtml = new StringWriter(256)

    script match {
      case PageScript(asset, _, mime) if inline =>
        assetsHtml.append("<script type=\"" + mime + "\">" + asset.asString + "</script>")

      case PageScript(asset, ext, mime) =>
        val file = new File(s"scripts/${UUID.randomUUID()}.$ext")
        writeAsset(asset, new File(s"$outputDir/$file"))
        assetsHtml.append("<script type=\"" + mime + "\" src=\"" + makeUrl(file) + "\"></script>")
    }

    style match {
      case PageStyle(asset, _, mime) if inline =>
        assetsHtml.append("<style type=\"" + mime + "\">" + asset.asString + "</style>")

      case PageStyle(asset, ext, mime) =>
        val file = new File(s"styles/${UUID.randomUUID()}.$ext")
        writeAsset(asset, new File(s"$outputDir/$file"))
        assetsHtml.append("<link rel=\"stylesheet\" href=\"" + makeUrl(file) + "\"/>")
    }

    for (PageFile(name, asset, ext, mime) <- static)
      writeAsset(asset, new File(s"$outputDir/$name.$ext"))

    val html = makeHtml(compilers, contents.collect { case h: PageHtml => h }, assetsHtml.toString)
    writeAsset(html.asset, new File(s"$outputDir/$name.html"))
  }
}

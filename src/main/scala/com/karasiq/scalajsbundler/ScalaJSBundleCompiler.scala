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
  private def compileAssets(compilers: AssetCompilers, contents: Seq[PageTypedContent]): String = {
    require(contents.forall(_.mime == contents.head.mime))
    val precompiler = contents.head.mime match {
      case "text/html" ⇒
        HtmlConcatCompiler

      case "text/css" | "text/javascript" ⇒
        ConcatCompiler

      case _ ⇒
        compilers(contents.head.mime)
    }
    precompiler.compile(contents)
  }

  private def writeAsset(asset: Asset, file: File): Unit = {
    require(file.getParentFile.isDirectory || file.getParentFile.mkdirs(), s"Not a directory: ${file.getParentFile}")
    val reader = asset.content()
    val outputStream = new FileOutputStream(file)
    Exception.allCatch.andFinally(IOUtils.closeQuietly(outputStream)) {
      IOUtils.write(IOUtils.toByteArray(reader), outputStream)
    }
  }

  private def makeUrl(file: File): String = {
    file.toString
      .split("[\\\\/]")
      .filter(_.nonEmpty)
      .mkString("/", "/", "")
  }

  private def makeHtml(compilers: AssetCompilers, pages: Seq[Seq[PageHtml]], assetsHtml: String): PageHtml = {
    def withAssets(html: String): String = {
      val page = Jsoup.parse(html)
      page.head().append(assetsHtml)
      page.outerHtml()
    }

    Html from compilers("text/html").compile(Seq(Html from withAssets(HtmlConcatCompiler.concat(pages.map { pages ⇒
      compileAssets(compilers, pages)
    }))))
  }

  private def makeScript(compilers: AssetCompilers, scripts: Seq[Seq[PageScript]]): PageScript = {
    Script from compilers("text/javascript").compile(Seq(Script from ConcatCompiler.concat(scripts.map { scripts ⇒
      compileAssets(compilers, scripts)
    })))
  }

  private def makeStyle(compilers: AssetCompilers, styles: Seq[Seq[PageStyle]]): PageStyle = {
    Style from compilers("text/css").compile(Seq(Style from ConcatCompiler.concat(styles.map { styles ⇒
      compileAssets(compilers, styles)
    })))
  }

  def createHtml(compilers: AssetCompilers, outputDir: String, name: String, contents: Seq[PageContent]): Unit = {
    require(new File(outputDir).isDirectory || new File(outputDir).mkdirs(), s"Not a directory: $outputDir")
    val script = makeScript(compilers, contents.collect { case s: PageScript ⇒ s }.groupBy(_.mime).values.toSeq)
    val style = makeStyle(compilers, contents.collect { case s: PageStyle ⇒ s }.groupBy(_.mime).values.toSeq)
    val static = contents.collect { case f: PageFile ⇒ f }
    val assetsHtml = new StringWriter(256)

    script match {
      case PageScript(asset, ext, mime) ⇒
        val file = new File(s"scripts/${UUID.randomUUID()}.$ext")
        writeAsset(asset, new File(s"$outputDir/$file"))
        assetsHtml.append("<script type=\"" + mime + "\" src=\"" + makeUrl(file) + "\"></script>")
    }

    style match {
      case PageStyle(asset, ext, mime) ⇒
        val file = new File(s"styles/${UUID.randomUUID()}.$ext")
        writeAsset(asset, new File(s"$outputDir/$file"))
        assetsHtml.append("<link rel=\"stylesheet\" href=\"" + makeUrl(file) + "\"/>")
    }

    for (PageFile(name, asset, ext, mime) <- static) {
      writeAsset(asset, new File(s"$outputDir/$name.$ext"))
    }

    val html = makeHtml(compilers, contents.collect { case h: PageHtml ⇒ h }.groupBy(_.mime).values.toSeq, assetsHtml.toString)
    writeAsset(html.asset, new File(s"$outputDir/$name.html"))
  }
}

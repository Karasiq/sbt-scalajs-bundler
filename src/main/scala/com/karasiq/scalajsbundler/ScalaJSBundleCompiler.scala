package com.karasiq.scalajsbundler

import java.io._
import java.util.UUID

import com.karasiq.scalajsbundler.ScalaJSBundler._
import com.karasiq.scalajsbundler.compilers.AssetCompilers
import org.apache.commons.io.IOUtils

import scala.util.control.Exception

class ScalaJSBundleCompiler {
  private def compileAssets(compilers: AssetCompilers, contents: Seq[PageTypedContent]): String = {
    require(contents.forall(_.mime == contents.head.mime))
    compilers(contents.head.mime).compile(contents)
  }

  private def writeAsset(asset: Asset, file: File): Unit = {
    require(file.getParentFile.isDirectory || file.getParentFile.mkdirs(), "Not a directory")
    val reader = asset.content()
    val outputStream = new FileOutputStream(file)
    Exception.allCatch.andFinally { outputStream.close() } {
      IOUtils.write(IOUtils.toByteArray(reader), outputStream)
    }
  }

  private def makeUrl(file: File): String = {
    file.toString.split("[\\\\/]").mkString("/", "/", "")
  }

  def createHtml(compilers: AssetCompilers, outputDir: String, name: String, contents: Seq[PageContent]): Unit = {
    require(new File(outputDir).isDirectory || new File(outputDir).mkdirs(), "Not a directory")

    val scripts = contents.collect { case s: PageScript ⇒ s }.groupBy(_.mime).mapValues { scripts ⇒
      PageScript(StringAsset(compileAssets(compilers, scripts)), scripts.head.ext, scripts.head.mime)
    }

    val styles = contents.collect { case s: PageStyle ⇒ s }.groupBy(_.mime).mapValues { styles ⇒
      PageStyle(StringAsset(compileAssets(compilers, styles)), styles.head.ext, styles.head.mime)
    }

    val static = contents.collect { case f: PageFile ⇒ f }

    val assetsHtml = new StringWriter(256)

    for (PageScript(asset, ext, mime) <- scripts.values) {
      val file = new File(s"scripts/${UUID.randomUUID()}.$ext")
      writeAsset(asset, new File(s"$outputDir/$file"))
      assetsHtml.append(s"<script type='$mime' src='${makeUrl(file)}'></script>")
    }

    for (PageStyle(asset, ext, mime) <- styles.values) {
      val file = new File(s"styles/${UUID.randomUUID()}.$ext")
      writeAsset(asset, new File(s"$outputDir/$file"))
      assetsHtml.append(s"<link rel='stylesheet' type='$mime' src='${makeUrl(file)}'/>")
    }

    for (PageFile(name, asset, ext, mime) <- static) {
      writeAsset(asset, new File(s"$outputDir/$name.$ext"))
    }

    val page = compileAssets(compilers, contents.collect { case h: PageHtml ⇒ h })
      .replaceAllLiterally("<generated-assets/>", assetsHtml.toString)

    writeAsset(StringAsset(page), new File(s"$outputDir/$name.html"))
  }
}

package com.karasiq.scalajsbundler

import java.io._
import java.util.UUID

import com.google.javascript.jscomp._
import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import com.karasiq.scalajsbundler.ScalaJSBundler._
import com.yahoo.platform.yui.compressor.CssCompressor
import org.apache.commons.io.IOUtils

import scala.collection.JavaConversions._
import scala.util.control.Exception

class ScalaJSBundleCompiler {
  private def compileAssets(contents: Seq[PageTypedContent]): String = {
    require(contents.forall(_.mime == contents.head.mime))
    val source = contents.map(_.asset.asString).mkString("\n")

    contents.head.mime match {
      case "text/javascript" ⇒
        val compiler = new Compiler()
        val options = new CompilerOptions
        CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options)
        val jsInput = SourceFile.fromCode("input.js", source)
        val result = compiler.compile(CommandLineRunner.getDefaultExterns, Seq(jsInput), options)
        result.errors.foreach(e ⇒ println(e.toString))
        assert(result.errors.isEmpty, "Compilation failed")
        result.warnings.foreach(e ⇒ println(e.toString))
        compiler.toSource

      case "text/css" ⇒
        val reader = new StringReader(source)
        val writer = new StringWriter(1024)
        val compressor = new CssCompressor(reader)
        compressor.compress(writer, -1)
        writer.toString

      case "text/html" ⇒
        val compressor = new HtmlCompressor
        compressor.compress(source)

      case _ ⇒
        source
    }
  }

  private def writeAsset(asset: Asset, file: File): Unit = {
    val reader = asset.content()
    val writer = new OutputStreamWriter(new FileOutputStream(file))
    Exception.allCatch.andFinally { writer.close(); reader.close() } {
      IOUtils.write(IOUtils.toByteArray(reader), writer)
    }
  }

  def createHtml(outputDir: String, name: String, contents: Seq[PageContent]): Unit = {
    require(new File(outputDir).mkdirs(), "Not a directory")

    val scripts = contents.collect { case s: PageScript ⇒ s }.groupBy(_.mime).mapValues { scripts ⇒
      PageScript(StringAsset(compileAssets(scripts)), scripts.head.ext, scripts.head.mime)
    }

    val styles = contents.collect { case s: PageStyle ⇒ s }.groupBy(_.mime).mapValues { styles ⇒
      PageStyle(StringAsset(compileAssets(styles)), styles.head.ext, styles.head.mime)
    }

    val static = contents.collect { case f: PageFile ⇒ f }

    val assetsHtml = new StringWriter(256)

    for (PageScript(asset, ext, mime) <- scripts.values) {
      val file = new File(s"$outputDir/scripts/${UUID.randomUUID()}.$ext")
      writeAsset(asset, file)
      assetsHtml.append(s"<script type=\"$mime\" src=\"/$file\"></script>")
    }

    for (PageStyle(asset, ext, mime) <- styles.values) {
      val file = new File(s"$outputDir/styles/${UUID.randomUUID()}.$ext")
      writeAsset(asset, file)
      assetsHtml.append(s"<link rel=\"stylesheet\" type=\"$mime\" src=\"/$file\"/>")
    }

    for (PageFile(name, asset, ext, mime) <- static) {
      writeAsset(asset, new File(s"$outputDir/$name.$ext"))
    }

    val page = compileAssets(contents.collect { case h: PageHtml ⇒ h })
      .replaceAllLiterally("<generated-assets/>", assetsHtml.toString)

    writeAsset(StringAsset(page), new File(s"$outputDir/$name.html"))
  }
}

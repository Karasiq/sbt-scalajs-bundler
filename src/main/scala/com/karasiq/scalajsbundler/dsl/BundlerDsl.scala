package com.karasiq.scalajsbundler.dsl

import java.io.File
import java.net.URL
import java.nio.file.Path

import com.karasiq.scalajsbundler.ScalaJSBundler._
import org.apache.commons.io.FilenameUtils

import scala.language.implicitConversions

trait BundlerDsl {
  object PageContentBuilders {
    sealed trait ContentBuilder[+T <: PageContent] {
      def fromAsset(asset: Asset): T
    }

    sealed trait TypedContentBuilder[+T <: PageTypedContent, B] extends ContentBuilder[T] {
      def withExt(ext: String): B
      def withMime(mime: String): B
    }

    sealed trait StaticContentBuilder[+T <: PageStaticContent, B] extends ContentBuilder[T] {
      def withName(name: String): B
    }

    case class ScriptBuilder(ext: String = "js", mime: String = "text/javascript") extends TypedContentBuilder[PageScript, ScriptBuilder] {
      override def withExt(ext: String): ScriptBuilder = copy(ext = ext)

      override def withMime(mime: String): ScriptBuilder = copy(mime = mime)

      override def fromAsset(asset: Asset): PageScript = PageScript(asset, this.ext, this.mime)
    }

    case class StyleBuilder(ext: String = "css", mime: String = "text/css") extends TypedContentBuilder[PageStyle, StyleBuilder] {
      override def withExt(ext: String): StyleBuilder = copy(ext = ext)

      override def withMime(mime: String): StyleBuilder = copy(mime = mime)

      override def fromAsset(asset: Asset): PageStyle = PageStyle(asset, this.ext, this.mime)
    }

    case class HtmlBuilder(ext: String = "html", mime: String = "text/html") extends TypedContentBuilder[PageHtml, HtmlBuilder] {
      override def withExt(ext: String): HtmlBuilder = copy(ext = ext)

      override def withMime(mime: String): HtmlBuilder = copy(mime = mime)

      override def fromAsset(asset: Asset): PageHtml = PageHtml(asset, this.ext, this.mime)
    }

    case class FileBuilder(name: String, ext: String, mime: String = "application/octet-stream") extends TypedContentBuilder[PageFile, FileBuilder] with StaticContentBuilder[PageFile, FileBuilder] {
      override def withName(name: String): FileBuilder = copy(name = name)

      override def withExt(ext: String): FileBuilder = copy(ext = ext)

      override def withMime(mime: String): FileBuilder = copy(mime = mime)

      override def fromAsset(asset: Asset): PageFile = PageFile(this.name, asset, this.ext, this.mime)
    }
  }

  import PageContentBuilders._

  def Script: ScriptBuilder = {
    ScriptBuilder()
  }

  def Style: StyleBuilder = {
    StyleBuilder()
  }

  def Html: HtmlBuilder = {
    HtmlBuilder()
  }

  def Static(name: String): FileBuilder = {
    FileBuilder(FilenameUtils.removeExtension(name), FilenameUtils.getExtension(name))
  }

  final implicit class BuilderOps[+T <: PageContent](val builder: ContentBuilder[T]) {
    def from[S](source: S)(implicit ev: S ⇒ Asset): T = {
      builder.fromAsset(source)
    }
  }

  implicit def urlToAsset(url: URL): Asset = WebAsset(url.toString)

  implicit def stringToAsset(str: String): Asset = StringAsset(str)

  implicit def fileToAsset(file: File): Asset = FileAsset(file.toString)

  implicit def pathToAsset(path: Path): Asset = FileAsset(path.toString)
}

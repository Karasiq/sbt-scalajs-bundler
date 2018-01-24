package com.karasiq.scalajsbundler.dsl

import javax.activation.MimetypesFileTypeMap

import scala.language.implicitConversions

import org.apache.commons.io.FilenameUtils

import com.karasiq.scalajsbundler.ScalaJSBundler._
import com.karasiq.scalajsbundler.compilers.PredefinedMimeTypes

trait BundlerDsl {
  object PageContentBuilders {
    sealed trait ContentBuilder[+T <: PageContent] {
      def fromAsset(asset: Asset): T
    }

    sealed trait TypedContentBuilder[+T <: PageTypedContent, +B] extends ContentBuilder[T] {
      def withExt(ext: String): B
      def withMime(mime: String): B
    }

    sealed trait StaticContentBuilder[+T <: PageStaticContent, +B] extends ContentBuilder[T] {
      def withName(name: String): B
    }

    case class InlineBuilder[+C <: PageTypedContent](ext: String, mime: String, f: (Asset, String, String) ⇒ C) extends TypedContentBuilder[C, InlineBuilder[C]] {
      override def withExt(ext: String): InlineBuilder[C] = {
        val map = new MimetypesFileTypeMap()
        copy(ext = ext, mime = map.getContentType(s"input.$ext"))
      }

      override def withMime(mime: String): InlineBuilder[C] = {
        copy(mime = mime)
      }

      override def fromAsset(asset: Asset): C = f(asset, ext, mime)
    }

    case class FileBuilder(name: String, ext: String, mime: String = "application/octet-stream") extends TypedContentBuilder[PageFile, FileBuilder] with StaticContentBuilder[PageFile, FileBuilder] {
      override def withName(name: String): FileBuilder = copy(name = name)

      override def withExt(ext: String): FileBuilder = copy(ext = ext)

      override def withMime(mime: String): FileBuilder = copy(mime = mime)

      override def fromAsset(asset: Asset): PageFile = PageFile(this.name, asset, this.ext, this.mime)
    }
  }

  import PageContentBuilders._

  def Script: InlineBuilder[PageScript] = {
    InlineBuilder("js", "text/javascript", PageScript.apply)
  }

  def Style: InlineBuilder[PageStyle] = {
    InlineBuilder("css", "text/css", PageStyle.apply)
  }

  def Html: InlineBuilder[PageHtml] = {
    InlineBuilder("html", "text/html", PageHtml.apply)
  }

  def Static(name: String): FileBuilder = {
    FileBuilder(FilenameUtils.removeExtension(name), "")
      .withExt(FilenameUtils.getExtension(name))
  }

  def Image(name: String): FileBuilder = {
    Static(name)
      .withMime("image/jpeg")
  }

  object Mimes extends PredefinedMimeTypes
}

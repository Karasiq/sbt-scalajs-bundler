package com.karasiq.scalajsbundler.dsl

import java.io.File
import java.net.URL
import java.nio.file.Path
import javax.activation.MimetypesFileTypeMap

import com.karasiq.scalajsbundler.ScalaJSBundler._
import com.karasiq.scalajsbundler.compilers.PredefinedMimeTypes
import org.apache.commons.io.FilenameUtils
import org.scalajs.sbtplugin.JarJSModuleID

import scala.language.implicitConversions

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

  final implicit class BuilderOps[+T <: PageContent](builder: ContentBuilder[T]) {
    def from[S](source: S)(implicit ev: S ⇒ Asset): T = {
      builder.fromAsset(source)
    }
  }

  implicit def urlToAsset(url: URL): Asset = WebAsset(url.toString)

  implicit def stringToAsset(str: String): Asset = StringAsset(str)

  implicit def fileToAsset(file: File): Asset = FileAsset(file.toString)

  implicit def pathToAsset(path: Path): Asset = FileAsset(path.toString)

  implicit def webjarToAsset(moduleId: JarJSModuleID): Asset = ResourceAsset(s"META-INF/resources/webjars/${moduleId.module.name}/${moduleId.module.revision}/${moduleId.jsDep.resourceName}")

  implicit def githubToAsset(gh: GithubRepository): Asset = WebAsset(gh.url)

  object Mimes extends PredefinedMimeTypes
}

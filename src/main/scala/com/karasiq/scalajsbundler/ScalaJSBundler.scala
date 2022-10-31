package com.karasiq.scalajsbundler

import java.io._

import com.karasiq.scalajsbundler.compilers.PredefinedMimeTypes
import org.apache.commons.io.IOUtils

import scala.util.control.Exception

object ScalaJSBundler {
  private object Mimes extends PredefinedMimeTypes

  trait Asset {
    def content(): InputStream

    def asString: String = {
      val reader = this.content()

      Exception.allCatch.andFinally(reader.close()) {
        new String(IOUtils.toByteArray(reader), "UTF-8")
      }
    }
  }

  case class FileAsset(path: String) extends Asset {
    override def content(): InputStream =
      new FileInputStream(path)
  }

  case class WebAsset(url: String) extends Asset {
    override def content(): InputStream =
      WebAssetsCache.inputStream(url)
  }

  case class StringAsset(data: String) extends Asset {
    override def content(): InputStream =
      new ByteArrayInputStream(data.getBytes("UTF-8"))
  }

  case class ResourceAsset(path: String) extends Asset {
    override def content(): InputStream =
      getClass.getClassLoader.getResourceAsStream(path)
  }

  sealed trait PageContent {
    def asset: Asset
  }

  sealed trait PageTypedContent extends PageContent {
    def ext: String
    def mime: String
  }

  sealed trait PageStaticContent extends PageContent {
    def name: String
  }

  case class PageFile(name: String, asset: Asset, ext: String, mime: String = Mimes.`octet-stream`)
      extends PageStaticContent with PageTypedContent

  object PageImage {
    def apply(name: String, asset: Asset, ext: String = "jpg", mime: String = Mimes.jpg): PageFile =
      PageFile(name, asset, ext, mime)
  }

  case class PageScript(asset: Asset, ext: String = "js", mime: String = Mimes.javascript) extends PageTypedContent

  case class PageStyle(asset: Asset, ext: String = "css", mime: String = Mimes.css) extends PageTypedContent

  case class PageHtml(asset: Asset, ext: String = "html", mime: String = Mimes.html) extends PageTypedContent

  case class Bundle(name: String, contents: Seq[PageContent]*)
}

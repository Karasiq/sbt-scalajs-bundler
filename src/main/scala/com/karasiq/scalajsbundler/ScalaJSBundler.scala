package com.karasiq.scalajsbundler

import java.io._

import org.apache.commons.io.IOUtils

import scala.io.Source
import scala.util.control.Exception

object ScalaJSBundler {
  sealed trait Asset {
    def content(): Reader

    def asString: String = {
      val reader = this.content()

      Exception.allCatch.andFinally(reader.close()) {
        new String(IOUtils.toByteArray(reader), "UTF-8")
      }
    }
  }

  case class FileAsset(path: String) extends Asset {
    override def content(): Reader = {
      Source.fromFile(path, "ASCII").bufferedReader()
    }
  }

  case class WebAsset(url: String) extends Asset {
    override def content(): Reader = {
      Source.fromURL(url, "ASCII").bufferedReader()
    }
  }

  case class StringAsset(data: String) extends Asset {
    override def content(): Reader = {
      new StringReader(data)
    }
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

  case class PageFile(name: String, asset: Asset, ext: String, mime: String = "application/octet-stream") extends PageStaticContent with PageTypedContent

  object PageImage {
    def apply(name: String, asset: Asset, ext: String = "jpg", mime: String = "image/jpeg"): PageFile = {
      PageFile(name, asset, ext, mime)
    }
  }

  case class PageScript(asset: Asset, ext: String = "js", mime: String = "text/javascript") extends PageTypedContent

  case class PageStyle(asset: Asset, ext: String = "css", mime: String = "text/css") extends PageTypedContent

  case class PageHtml(asset: Asset, ext: String = "html", mime: String = "text/html") extends PageTypedContent

  case class Bundle(name: String, contents: Seq[PageContent])
}

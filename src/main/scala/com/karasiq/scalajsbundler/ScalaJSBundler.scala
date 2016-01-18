package com.karasiq.scalajsbundler

import java.io.{File, _}
import java.net.URL

import org.apache.commons.io.IOUtils

import scala.util.Try
import scala.util.control.Exception
import scalaj.http.{Http, HttpOptions}

object ScalaJSBundler {
  sealed trait Asset {
    def content(): InputStream

    def asString: String = {
      val reader = this.content()

      Exception.allCatch.andFinally(reader.close()) {
        new String(IOUtils.toByteArray(reader), "UTF-8")
      }
    }
  }

  case class FileAsset(path: String) extends Asset {
    override def content(): InputStream = {
      new FileInputStream(path)
    }
  }

  case class WebAsset(url: String) extends Asset {
    private val cacheDir = "target/cached-webassets"

    private val cachedName: String = {
      val (host, file) = new URL(url) match { case u ⇒
        u.getHost → u.getFile
      }

      s"${Integer.toHexString(host.hashCode)}/${Integer.toHexString(file.hashCode)}"
    }

    private def cached: Option[File] = {
      Try(new File(s"$cacheDir/$cachedName"))
        .filter(f ⇒ f.exists() && f.isFile && f.length() > 0)
        .toOption
    }

    override def content(): InputStream = {
      cached match {
        case Some(file) ⇒
          new FileInputStream(file)

        case None ⇒
          val http = Http(url).options(HttpOptions.connTimeout(10000), HttpOptions.readTimeout(10000)).asBytes
          assert(http.isSuccess, s"Web asset download failed: $url")
          val bytes = http.body
          val outputStream = Try {
            val file = new File(s"$cacheDir/$cachedName")
            require(file.getParentFile.isDirectory || file.getParentFile.mkdirs(), s"Not a directory: ${file.getParentFile}")
            new FileOutputStream(file)
          }
          outputStream.foreach { stream ⇒
            Exception.allCatch.andFinally(IOUtils.closeQuietly(stream)) {
              IOUtils.write(bytes, stream)
            }
          }
          new ByteArrayInputStream(bytes)
      }
    }
  }

  case class StringAsset(data: String) extends Asset {
    override def content(): InputStream = {
      new ByteArrayInputStream(data.getBytes("UTF-8"))
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

  case class Bundle(name: String, contents: PageContent*)
}

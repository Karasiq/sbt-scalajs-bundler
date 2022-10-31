package com.karasiq.scalajsbundler

import java.io._
import java.net.URL

import org.apache.commons.io.IOUtils

import scala.util.Try
import scala.util.control.Exception
import scalaj.http.{Http, HttpOptions}

object WebAssetsCache {
  private val cacheDir = "target/cached-webassets"

  def inputStream(url: String): InputStream = {
    val cachedName: String = {
      val (host, file) = new URL(url) match { case u => u.getHost -> u.getFile }

      s"${Integer.toHexString(host.hashCode)}/${Integer.toHexString(file.hashCode)}"
    }

    val cached: Option[File] =
      Try(new File(s"$cacheDir/$cachedName"))
        .filter(f => f.exists() && f.isFile && f.length() > 0)
        .toOption

    cached match {
      case Some(file) => new FileInputStream(file)

      case None =>
        val http = Http(url).options(HttpOptions.connTimeout(10000), HttpOptions.readTimeout(10000)).asBytes
        assert(http.isSuccess, s"Web asset download failed: $url")
        val bytes = http.body
        val outputStream =
          Try {
            val file = new File(s"$cacheDir/$cachedName")
            require(
              file.getParentFile.isDirectory || file.getParentFile.mkdirs(),
              s"Not a directory: ${file.getParentFile}"
            )
            new FileOutputStream(file)
          }
        outputStream.foreach { stream =>
          Exception.allCatch.andFinally(IOUtils.closeQuietly(stream)) {
            IOUtils.write(bytes, stream)
          }
        }
        new ByteArrayInputStream(bytes)
    }
  }

  def text(url: String): String = {
    val inputStream = WebAssetsCache.inputStream(url)
    Exception.allCatch.andFinally(IOUtils.closeQuietly(inputStream)) {
      IOUtils.toString(inputStream, "UTF-8")
    }
  }
}

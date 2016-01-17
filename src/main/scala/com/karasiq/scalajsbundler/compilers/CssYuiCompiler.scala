package com.karasiq.scalajsbundler.compilers

import java.io.{StringReader, StringWriter}

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent
import com.yahoo.platform.yui.compressor.CssCompressor

object CssYuiCompiler extends AssetCompiler {
  override def compile(contents: Seq[PageTypedContent]): String = {
    val reader = new StringReader(ConcatCompiler.compile(contents))
    val writer = new StringWriter(1024)
    val compressor = new CssCompressor(reader)
    compressor.compress(writer, -1)
    writer.toString
  }
}

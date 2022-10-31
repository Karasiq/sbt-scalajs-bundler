package com.karasiq.scalajsbundler.compilers

import java.io.{StringReader, StringWriter}

import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor
import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent
import com.yahoo.platform.yui.compressor.JavaScriptCompressor

class JsYuiCompiler extends AssetCompiler {
  override def compile(contents: Seq[PageTypedContent]): String = {
    val reader     = new StringReader(ConcatCompiler.compile(contents))
    val writer     = new StringWriter(1024)
    val compressor = new JavaScriptCompressor(reader, new YuiJavaScriptCompressor.DefaultErrorReporter)
    compressor.compress(writer, -1, true, false, false, false)
    writer.toString
  }
}

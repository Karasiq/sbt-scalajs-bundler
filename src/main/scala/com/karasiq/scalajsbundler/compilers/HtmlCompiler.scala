package com.karasiq.scalajsbundler.compilers

import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

object HtmlCompiler extends AssetCompiler {
  override def compile(contents: Seq[PageTypedContent]): String = {
    val compressor = new HtmlCompressor
    compressor.compress(ConcatCompiler.compile(contents))
  }
}

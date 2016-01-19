package com.karasiq.scalajsbundler.compilers

import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

class HtmlGoogleCompiler extends AssetCompiler {
  override def compile(contents: Seq[PageTypedContent]): String = {
    val compressor = new HtmlCompressor
    compressor.compress(HtmlConcatCompiler.compile(contents))
  }
}

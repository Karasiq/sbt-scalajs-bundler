package com.karasiq.scalajsbundler.compilers

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

trait AssetCompiler {
  def compile(contents: Seq[PageTypedContent]): String
}

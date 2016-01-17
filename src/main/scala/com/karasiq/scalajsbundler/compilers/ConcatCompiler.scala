package com.karasiq.scalajsbundler.compilers

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

object ConcatCompiler extends AssetCompiler {
  override def compile(contents: Seq[PageTypedContent]): String = {
    contents.map(_.asset.asString).mkString("\n")
  }
}

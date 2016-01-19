package com.karasiq.scalajsbundler.compilers

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

object ConcatCompiler extends AssetCompiler {
  def concat(contents: Seq[String]): String = {
    contents.mkString("\n")
  }

  override def compile(contents: Seq[PageTypedContent]): String = {
    concat(contents.map(_.asset.asString))
  }
}

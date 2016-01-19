package com.karasiq.scalajsbundler.compilers

import com.github.sommeri.less4j.core.DefaultLessCompiler
import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

import scala.language.postfixOps

class Less4jCompiler extends AssetCompiler {
  override def compile(contents: Seq[PageTypedContent]): String = {
    val compiler = new DefaultLessCompiler
    compiler.compile(ConcatCompiler.compile(contents)).getCss
  }
}

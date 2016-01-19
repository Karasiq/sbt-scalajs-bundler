package com.karasiq.scalajsbundler.compilers

import com.google.javascript.jscomp._
import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

import scala.collection.JavaConversions._

class JsClosureCompiler extends AssetCompiler {
  //noinspection ScalaDeprecation
  override def compile(contents: Seq[PageTypedContent]): String = {
    val compiler = new Compiler()
    val options = new CompilerOptions
    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options)
    val jsInput = contents.zipWithIndex.map { case (c, index) ⇒
      SourceFile.fromCode(s"input_$index.js", c.asset.asString)
    }
    val result = compiler.compile(CommandLineRunner.getDefaultExterns, jsInput, options)
    // result.errors.foreach(e ⇒ println(e.toString))
    assert(result.errors.isEmpty, "Compilation failed")
    //result.warnings.foreach(e ⇒ println(e.toString))
    compiler.toSource
  }
}

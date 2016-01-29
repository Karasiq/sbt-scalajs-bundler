package com.karasiq.scalajsbundler.compilers

import java.nio.charset.Charset

import com.google.javascript.jscomp._
import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

import scala.collection.JavaConversions._

class JsClosureCompiler(advanced: Boolean) extends AssetCompiler {
  def this() = {
    this(advanced = false)
  }

  //noinspection ScalaDeprecation
  override def compile(contents: Seq[PageTypedContent]): String = {
    // Google Closure Compiler
    val compiler = new Compiler()

    // Set options
    val options = new CompilerOptions
    val level = if (advanced) {
      CompilationLevel.ADVANCED_OPTIMIZATIONS
    } else {
      CompilationLevel.SIMPLE_OPTIMIZATIONS
    }
    level.setOptionsForCompilationLevel(options)

    // Compile input files
    val jsInput = contents.zipWithIndex.map { case (c, index) ⇒
      SourceFile.fromInputStream(s"input_$index.js", c.asset.content(), Charset.forName("UTF-8"))
    }
    val result = compiler.compile(CommandLineRunner.getDefaultExterns, jsInput, options)
    assert(result.errors.isEmpty, "Compilation failed")
    compiler.toSource
  }
}

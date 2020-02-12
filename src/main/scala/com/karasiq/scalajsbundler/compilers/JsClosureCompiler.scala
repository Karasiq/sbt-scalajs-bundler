package com.karasiq.scalajsbundler.compilers

import scala.collection.JavaConverters._

import com.google.javascript.jscomp._
import com.google.javascript.jscomp.CompilerOptions.LanguageMode

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

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
    options.setLanguageIn(LanguageMode.STABLE_IN)
    options.setLanguageOut(LanguageMode.STABLE_OUT)

    val level = if (advanced) {
      //CompilationLevel.ADVANCED_OPTIMIZATIONS // must track global refs for this to work
      CompilationLevel.SIMPLE_OPTIMIZATIONS
    } else {
      CompilationLevel.SIMPLE_OPTIMIZATIONS
    }
    level.setOptionsForCompilationLevel(options)

    // Compile input files
    val jsInput = contents.zipWithIndex.map { case (c, index) ⇒
      SourceFile.fromInputStream(s"input_$index.js", c.asset.content())
    }
    val result = compiler.compile(CommandLineRunner.getDefaultExterns, jsInput.asJava, options)
    require(result.errors.isEmpty, "Compilation failed")
    compiler.toSource
  }
}

package com.karasiq.scalajsbundler.compilers

import scala.collection.JavaConverters._

import com.google.javascript.jscomp._
import com.google.javascript.jscomp.CompilerOptions.LanguageMode

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

class JsClosureCompiler(advanced: Boolean, langIn: LanguageMode, langOut: LanguageMode) extends AssetCompiler {
  def this() =
    this(advanced = false, LanguageMode.STABLE_IN, LanguageMode.STABLE_OUT)

  // noinspection ScalaDeprecation
  override def compile(contents: Seq[PageTypedContent]): String = {
    // Google Closure Compiler
    val compiler = new Compiler()

    // Set options
    val options = new CompilerOptions
    options.setLanguageIn(langIn)
    options.setLanguageOut(langOut)

    val level =
      if (advanced) {
        CompilationLevel.ADVANCED_OPTIMIZATIONS
      } else {
        CompilationLevel.SIMPLE_OPTIMIZATIONS
      }
    level.setOptionsForCompilationLevel(options)

    // Compile input files
    val jsInput =
      contents.zipWithIndex.map { case (c, index) => SourceFile.fromInputStream(s"input_$index.js", c.asset.content()) }
    val result = compiler.compile(CommandLineRunner.getDefaultExterns, jsInput.asJava, options)
    require(result.errors.isEmpty, "Compilation failed")
    compiler.toSource
  }
}

object JsClosureCompiler {
  def apply(
      advanced: Boolean = false,
      langIn: LanguageMode = LanguageMode.STABLE_IN,
      langOut: LanguageMode = LanguageMode.STABLE_OUT
    ): JsClosureCompiler =
    new JsClosureCompiler(advanced, langIn, langOut)

  def default: JsClosureCompiler = new JsClosureCompiler()
}

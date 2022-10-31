package com.karasiq.scalajsbundler.compilers

import javax.script.{ScriptContext, ScriptEngineManager, SimpleScriptContext}

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent

trait JsEngineAssetCompiler extends AssetCompiler {
  protected def scripts: Seq[String]

  protected def compilerCall(inputVar: String): String

  protected final val context = new SimpleScriptContext

  protected final lazy val jsEngine = {
    val jsEngine = new ScriptEngineManager(null).getEngineByExtension("js")
    assert(jsEngine.ne(null), "No JavaScript engine found in classpath")
    scripts.foreach(script => jsEngine.eval(script, context))
    jsEngine
  }

  override def compile(contents: Seq[PageTypedContent]): String = {
    context.setAttribute("_script_source", ConcatCompiler.compile(contents), ScriptContext.ENGINE_SCOPE)
    jsEngine.eval(s"${this.compilerCall("_script_source")};", context).asInstanceOf[String]
  }
}

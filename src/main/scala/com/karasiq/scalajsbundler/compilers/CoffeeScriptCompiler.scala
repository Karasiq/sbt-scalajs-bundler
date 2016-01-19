package com.karasiq.scalajsbundler.compilers

import javax.script.{ScriptContext, ScriptEngineManager, SimpleScriptContext}

import com.karasiq.scalajsbundler.ScalaJSBundler.PageTypedContent
import com.karasiq.scalajsbundler.WebAssetsCache

object CoffeeScriptCompiler extends AssetCompiler {
  private def compilerSource(): String = {
    WebAssetsCache.text("http://coffeescript.org/extras/coffee-script.js")
  }

  // Initialization
  private val jsEngine = new ScriptEngineManager().getEngineByExtension("js")
  assert(jsEngine.ne(null), "No JavaScript engine found in classpath")
  private val context = new SimpleScriptContext
  jsEngine.eval(compilerSource(), context)

  override def compile(contents: Seq[PageTypedContent]): String = {
    context.setAttribute("_script_source", ConcatCompiler.compile(contents), ScriptContext.ENGINE_SCOPE)
    jsEngine.eval("CoffeeScript.compile(_script_source);", context).asInstanceOf[String]
  }
}

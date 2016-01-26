package com.karasiq.scalajsbundler.compilers

import com.karasiq.scalajsbundler.WebAssetsCache

object CoffeeScriptCompiler extends JsEngineAssetCompiler {
  override protected def scripts: Seq[String] = Seq {
    WebAssetsCache.text("http://coffeescript.org/extras/coffee-script.js")
  }

  override protected def compilerCall(inputVar: String): String = {
    s"CoffeeScript.compile($inputVar)"
  }
}

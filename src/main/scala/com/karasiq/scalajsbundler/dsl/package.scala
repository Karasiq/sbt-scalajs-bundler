package com.karasiq.scalajsbundler

package object dsl extends BundlerDsl with AssetShortcuts {
  type Bundle = ScalaJSBundler.Bundle
  val Bundle: ScalaJSBundler.Bundle.type = ScalaJSBundler.Bundle
}

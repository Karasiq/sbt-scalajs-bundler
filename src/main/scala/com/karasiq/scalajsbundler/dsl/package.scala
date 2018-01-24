package com.karasiq.scalajsbundler

package object dsl extends BundlerDsl with BundlerImplicits with AssetShortcuts {
  type Bundle = ScalaJSBundler.Bundle
  val Bundle: ScalaJSBundler.Bundle.type = ScalaJSBundler.Bundle
}

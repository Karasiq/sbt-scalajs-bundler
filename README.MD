# sbt-scalajs-bundler
SBT plugin for auto compilation of web assets (js, css, html)

# How to use
Add line in your `plugins.sbt`:
```scala
addSbtPlugin("com.github.karasiq" % "sbt-scalajs-bundler" % "1.0.0")
```

Write configuration in `build.sbt`:
```scala
// Enable bundler plugin
enablePlugins(ScalaJSBundlerPlugin)

// Assuming frontend is your Scala.js project, and this file configures backend package
scalaJsBundlerCompile in Compile <<= (scalaJsBundlerCompile in Compile).dependsOn(fullOptJS in Compile in frontend)

// Assets will be compiled to "webapp" resources directory
import com.github.karasiq.scalajsbundler.ScalaJSBundler._
scalaJsBundlerAssets in Compile += Bundle("index",
  // jQuery
  PageScript(WebAsset("https://code.jquery.com/jquery-1.12.0.js")),

  // Bootstrap
  PageStyle(WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/css/bootstrap.css")),
  PageScript(WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/js/bootstrap.js")),
  PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.eot"), "eot"),
  PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.svg"), "svg"),
  PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.ttf"), "ttf"),
  PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.woff"), "woff"),
  PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.woff2"), "woff2"),

  // Static files
  PageHtml(FileAsset("frontend/webapp/html/index.html")),
  PageStyle(FileAsset("frontend/webapp/css/style.css")),
  PageImage("favicon", FileAsset("frontend/webapp/img/favicon.ico"), "ico", "image/x-icon"),

  // Scala.js app
  PageScript(FileAsset("frontend/target/scala-2.11/your-app-frontend-opt.js")),
  PageScript(FileAsset("frontend/target/scala-2.11/your-app-frontend-launcher.js"))
)
```
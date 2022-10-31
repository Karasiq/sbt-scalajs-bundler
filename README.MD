# sbt-scalajs-bundler  [![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.29.svg)](https://www.scala-js.org) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.karasiq/sbt-scalajs-bundler/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.karasiq/sbt-scalajs-bundler)
SBT plugin for auto compilation of web assets (js, css, html)

# How to use
Add line in your `plugins.sbt`:
```scala
addSbtPlugin("com.github.karasiq" % "sbt-scalajs-bundler" % "%version%")
```

Write configuration in `build.sbt`:
```scala
// Enable bundler plugin
enablePlugins(SJSAssetBundlerPlugin)

// Assets will be compiled to "webapp" resources directory
Compile / scalaJsBundlerAssets ++= {
  import com.karasiq.scalajsbundler.dsl.{Script, _}
  val bootstrap = github("twbs", "bootstrap", "v3.3.6") / "dist"
  val jsDeps = Seq(
    Script from "org.webjars" % "jquery" % "2.1.3" / "jquery.js", // Requires corresponding entry in libraryDependencies
    Style from bootstrap / "css" / "bootstrap.css",
    Script from bootstrap / "js" / "bootstrap.js"
  )

  val staticDir = (baseDirectory in frontend)(_ / "webapp").value
  val staticFiles = Seq(
    // Static files
    Html from staticDir / "html" / "index.html",
    Script.withMime(Mimes.coffeescript) from staticDir / "scripts" / "script.coffee",
    Style.withMime(Mimes.less) from staticDir / "css" / "style.less", // Requires less4j library in classpath
    Image("favicon.ico").withMime(Mimes.ico) from staticDir / "img" / "favicon.ico"
  )


  Bundle("index", jsDeps, (bootstrap / "fonts" / "glyphicons-halflings-regular").fonts(), staticFiles, SJSApps.app(frontend).value)
}
```

# Examples
* [Nanoboard frontend](https://github.com/Karasiq/nanoboard)
* [scalajs-videojs test page](https://github.com/Karasiq/scalajs-videojs)
* [scalajs-bootstrap test page](https://github.com/Karasiq/scalajs-bootstrap)
* [scalajs-highcharts test page](https://github.com/Karasiq/scalajs-highcharts)

# License
The MIT License (MIT)

Copyright (c) 2016 Karasiq

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

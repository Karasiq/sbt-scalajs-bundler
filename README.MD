# sbt-scalajs-bundler [![Build Status](https://travis-ci.org/Karasiq/sbt-scalajs-bundler.svg?branch=master)](https://travis-ci.org/Karasiq/sbt-scalajs-bundler)
SBT plugin for auto compilation of web assets (js, css, html)

# How to use
Add line in your `plugins.sbt`:
```scala
addSbtPlugin("com.github.karasiq" % "sbt-scalajs-bundler" % "1.0.4")
```

Write configuration in `build.sbt`:
```scala
import com.karasiq.scalajsbundler.dsl.{Script, _}

// Enable bundler plugin
enablePlugins(ScalaJSBundlerPlugin)

// Assuming frontend is your Scala.js project, and this file configures backend package
scalaJsBundlerCompile in Compile <<= (scalaJsBundlerCompile in Compile).dependsOn(fullOptJS in Compile in frontend)

// Assets will be compiled to "webapp" resources directory
scalaJsBundlerAssets in Compile += Bundle("index",
  // jQuery
  Script from url("https://code.jquery.com/jquery-1.12.0.js"),

  // Bootstrap
  Style from url("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/css/bootstrap.css"),
  Script from url("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/js/bootstrap.js"),
  Static("fonts/glyphicons-halflings-regular.eot") from url("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.eot"),
  Static("fonts/glyphicons-halflings-regular.svg") from url("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.svg"),
  Static("fonts/glyphicons-halflings-regular.ttf") from url("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.ttf"),
  Static("fonts/glyphicons-halflings-regular.woff") from url("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.woff"),
  Static("fonts/glyphicons-halflings-regular.woff2") from url("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.woff2"),

  // Static files
  Html from file("frontend/webapp/html/index.html"),
  Script.withMime(Mimes.coffeescript) from file("frontend/webapp/scripts/script.coffee"),
  Style.withMime(Mimes.less) from file("frontend/webapp/css/style.less"), // Requires less4j library in classpath
  Image("favicon.ico").withMime(Mimes.ico) from file("frontend/webapp/img/favicon.ico"),

  // Scala.js app
  Script from file("frontend/target/scala-2.11/your-app-frontend-opt.js"),
  Script from file("frontend/target/scala-2.11/your-app-frontend-launcher.js")
)
```

# Examples
* [scalajs-videojs test page](https://github.com/Karasiq/scalajs-videojs)
* [scalajs-bootstrap test page](https://github.com/Karasiq/scalajs-bootstrap)
* [scalajs-highcharts test page](https://github.com/Karasiq/scalajs-highcharts)
* [WebM-TV](https://github.com/Karasiq/webm-tv)

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

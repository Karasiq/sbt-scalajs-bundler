# sbt-scalajs-bundler
SBT plugin for auto compilation of web assets (js, css, html)

# How to use
Add line in your `plugins.sbt`:
```scala
addSbtPlugin("com.github.karasiq" % "sbt-scalajs-bundler" % "1.0.3")
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
  Script.withMime("text/x-coffeescript") from file("frontend/webapp/scripts/script.coffee"),
  Style.withMime("text/less") from file("frontend/webapp/css/style.less"), // Requires less4j library in classpath
  Image("favicon.ico").withMime("image/x-icon") from file("frontend/webapp/img/favicon.ico"),

  // Scala.js app
  Script from file("frontend/target/scala-2.11/your-app-frontend-opt.js"),
  Script from file("frontend/target/scala-2.11/your-app-frontend-launcher.js")
)
```

# Examples
* [WebM-TV](https://github.com/Karasiq/webm-tv)

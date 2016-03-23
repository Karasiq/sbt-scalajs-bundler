import java.io.{FileInputStream, IOException}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.karasiq.scalajsbundler.ScalaJSBundleCompiler
import com.karasiq.scalajsbundler.compilers.{AssetCompilers, JsClosureCompiler}
import com.karasiq.scalajsbundler.dsl.{Script, _}
import org.apache.commons.io.IOUtils
import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport._
import org.scalatest.{FlatSpec, Matchers}
import sbt.{url, _}

import scala.util.Try

class ScalaJSBundlerTest extends FlatSpec with Matchers {
  val output = "target/test-output"

  def readFile(file: String): String = {
    val inputStream = new FileInputStream(file)
    val result = Try(IOUtils.toString(inputStream))
    IOUtils.closeQuietly(inputStream)
    result.getOrElse("")
  }

  "WebJar resources" should "be extracted" in {
    getClass.getClassLoader.getResource("META-INF/resources/webjars/jquery/2.1.3/jquery.js") should not be null
  }

  "Assets compiler" should "compile assets" in {
    val assets = Seq(
      // jQuery
      Script from github("jquery", "jquery", "1.12.0") / "dist" / "jquery.js",

      // Font awesome
      Static("fonts/fontawesome-webfont.woff2")
        .withMime("application/font-woff2") from url("https://fortawesome.github.io/Font-Awesome/assets/font-awesome/fonts/fontawesome-webfont.woff2?v=4.5.0"),

      // Bootstrap
      Style from "org.webjars" % "bootstrap" % "3.3.6" / "css/bootstrap.css",

      // Page static files
      Script from """
                  |function hello(name) {
                  |  alert('Hello ' + name);
                  |}
                  |$(function() {
                  |  hello('JavaScript!');
                  |});
                """.stripMargin,
      Script
        .withMime(Mimes.coffeescript) from
          """
            |# Assignment:
            |number   = 42
            |opposite = true
            |
            |# Conditions:
            |number = -42 if opposite
            |
            |# Functions:
            |square = (x) -> x * x
            |
            |# Arrays:
            |list = [1, 2, 3, 4, 5]
            |
            |# Objects:
            |math =
            |  root:   Math.sqrt
            |  square: square
            |  cube:   (x) -> x * square x
            |
            |# Splats:
            |race = (winner, runners...) ->
            |  print winner, runners
            |
            |# Existence:
            |alert "I knew it!" if elvis?
            |
            |# Array comprehensions:
            |cubes = (math.cube num for num in list)
          """.stripMargin,
      Style from """
                   |.hello-world {
                   | font-family: Gill Sans, Verdana;
                   |	font-size: 11px;
                   |	line-height: 14px;
                   |	text-transform: uppercase;
                   |	letter-spacing: 2px;
                   |	font-weight: bold;
                   |};
                 """.stripMargin,
      Style
        .withMime(Mimes.less) from
        """
          |.class { width: (1 + 1) }
        """.stripMargin,
      Html from """
                  |<!DOCTYPE html>
                  |<html>
                  |<head>
                  |<title>Hello world</title>
                  |</head>
                  |<body style='font-family: Geneva, Arial, Helvetica, sans-serif'>
                  |<h1 class="hello-world">Hello world!</h1>
                  |</body>
                  |</html>
                """.stripMargin,
      Html
        .withMime(Mimes.jade) from """
                  |html
                  |  head
                  |  body(style='background: url(/background.jpg);')
                  |    h2.hello-jade Hello jade!
                """.stripMargin
    )

    if (Files.isDirectory(Paths.get(output))) {
      Files.walkFileTree(Paths.get(output), new SimpleFileVisitor[Path] {
        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        }

        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }
      })
    }
    
    val compiler = new ScalaJSBundleCompiler
    compiler.createHtml(AssetCompilers.default, output, "index", assets, inline = true)
    readFile(s"$output/index.html").hashCode shouldBe -1691038017
    Files.size(Paths.get(s"$output/fonts/fontawesome-webfont.woff2")) shouldBe 66624

    val fullOptCompilers = AssetCompilers {
      case Mimes.javascript ⇒
        new JsClosureCompiler(advanced = true)
    } <<= AssetCompilers.default

    compiler.createHtml(fullOptCompilers, output, "index_fullopt", assets, inline = true)
    readFile(s"$output/index_fullopt.html").hashCode shouldBe 1286016232
  }
}

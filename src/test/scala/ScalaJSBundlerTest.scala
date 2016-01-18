import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.karasiq.scalajsbundler.ScalaJSBundleCompiler
import com.karasiq.scalajsbundler.compilers.AssetCompilers
import com.karasiq.scalajsbundler.dsl._
import org.scalatest.{FlatSpec, Matchers}
import sbt.url

class ScalaJSBundlerTest extends FlatSpec with Matchers {
  val output = "target/test-output"

  "Assets compiler" should "compile assets" in {
    val assets = Seq(
      // jQuery
      Script from url("https://code.jquery.com/jquery-1.12.0.js"),

      // Font awesome
      Static("fonts/fontawesome-webfont.woff2")
        .withMime("application/font-woff2") from url("https://fortawesome.github.io/Font-Awesome/assets/font-awesome/fonts/fontawesome-webfont.woff2?v=4.5.0"),

      // Bootstrap
      Style from url("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/css/bootstrap.css"),

      // Page static files
      Script from """
                  |function hello(name) {
                  |  alert('Hello, ' + name);
                  |}
                  |$(function() {
                  |  hello('New user');
                  |});
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
      Html from """
                  |<!DOCTYPE html>
                  |<html>
                  |<head>
                  |<title>Hello world</title>
                  |<generated-assets/>
                  |</head>
                  |<body>
                  |<h1 class="hello-world">Hello world!</h1>
                  |</body>
                  |</html>
                """.stripMargin
    )

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
    val compiler = new ScalaJSBundleCompiler
    compiler.createHtml(AssetCompilers.default, output, "index", assets)
  }
}

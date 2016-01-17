import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.karasiq.scalajsbundler.ScalaJSBundleCompiler
import com.karasiq.scalajsbundler.ScalaJSBundler._
import com.karasiq.scalajsbundler.compilers.AssetCompilers
import org.scalatest.{FlatSpec, Matchers}

class ScalaJSBundlerTest extends FlatSpec with Matchers {
  val output = "target/test-output"

  "Assets compiler" should "compile assets" in {
    val script = PageScript(StringAsset(
      """
        |function hello(name) {
        |  alert('Hello, ' + name);
        |}
        |$(function() {
        |  hello('New user');
        |});
      """.stripMargin))

    val style = PageStyle(StringAsset(
      """
        |.hello-world {
        | font-family: Gill Sans, Verdana;
        |	font-size: 11px;
        |	line-height: 14px;
        |	text-transform: uppercase;
        |	letter-spacing: 2px;
        |	font-weight: bold;
        |};
      """.stripMargin))

    val html = PageHtml(StringAsset(
      """
        |<head>
        |<title>Hello world</title>
        |<generated-assets/>
        |</head>
        |<body>
        |<h1 class="hello-world">Hello world!</h1>
        |</body>
      """.stripMargin))

    val jquery = PageScript(WebAsset("https://code.jquery.com/jquery-1.12.0.js"))

    val font = PageFile("fonts/fontawesome-webfont", WebAsset("https://fortawesome.github.io/Font-Awesome/assets/font-awesome/fonts/fontawesome-webfont.woff2?v=4.5.0"), "woff2", "application/font-woff2")

    val bootstrap = PageStyle(WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/css/bootstrap.css"))

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
    compiler.createHtml(AssetCompilers.default, output, "index", Seq(jquery, bootstrap, style, font, script, html))
  }
}

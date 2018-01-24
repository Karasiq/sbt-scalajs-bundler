package com.karasiq.scalajsbundler.dsl

import java.io.File
import java.net.URL
import java.nio.file.Path

import scala.language.implicitConversions

import com.karasiq.scalajsbundler.ScalaJSBundler._

trait BundlerImplicits { self: BundlerDsl ⇒
  import PageContentBuilders._

  final implicit class BuilderOps[+T <: PageContent](builder: ContentBuilder[T]) {
    def from[S](source: S)(implicit ev: S ⇒ Asset): T = {
      builder.fromAsset(source)
    }
  }

  implicit def pageContentAsSeq[T <: PageContent](pc: T): Seq[PageContent] = Seq(pc)

  implicit def urlToAsset(url: URL): Asset = WebAsset(url.toString)

  implicit def stringToAsset(str: String): Asset = StringAsset(str)

  implicit def fileToAsset(file: File): Asset = FileAsset(file.toString)

  implicit def pathToAsset(path: Path): Asset = FileAsset(path.toString)

  implicit def webjarToAsset(moduleId: JarResource): Asset = ResourceAsset(s"META-INF/resources/webjars/${moduleId.module.name}/${moduleId.module.revision}/${moduleId.resourceName}")

  implicit def githubToAsset(gh: GithubRepository): Asset = WebAsset(gh.url)
}

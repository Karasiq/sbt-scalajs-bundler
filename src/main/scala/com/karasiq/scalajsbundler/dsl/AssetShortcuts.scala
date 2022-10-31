package com.karasiq.scalajsbundler.dsl

import com.karasiq.scalajsbundler.ScalaJSBundler

import java.net.URL
import org.apache.commons.io.FilenameUtils
import sbt._
import sbt.Keys._
import com.karasiq.scalajsbundler.ScalaJSBundler.{PageContent, ResourceAsset}
import org.scalajs.sbtplugin.ScalaJSPlugin.{autoImport => ScalaJS}
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.{autoImport => SJSBundler}

//noinspection ScalaDeprecation,DuplicatedCode
trait AssetShortcuts { self: BundlerDsl with BundlerImplicits =>
  def fontPackage(
      name: String,
      baseUrl: String,
      dir: String = "fonts",
      extensions: Seq[String] = Seq("eot", "svg", "ttf", "woff", "woff2")
    ): Seq[PageContent] =
    extensions.map { ext =>
      Static(s"$dir/$name.$ext") from new URL(s"$baseUrl.$ext")
    }

  implicit class GithubRepositoryOps(gh: GithubRepository) {
    def fonts(
        name: String = "",
        dir: String = "fonts",
        extensions: Seq[String] = Seq("eot", "svg", "ttf", "woff", "woff2")
      ): Seq[PageContent] = {
      val fontName = Option(name).filter(_.nonEmpty).getOrElse(FilenameUtils.getBaseName(gh.path.last))
      fontPackage(fontName, gh.url, dir, extensions)
    }
  }

  implicit class JarResourceOps(moduleId: ModuleID) {
    def /(resourceName: String): JarResource =
      JarResource(moduleId, resourceName)
  }

  implicit class URLOps(url: URL) {
    private[this] def urlWithTrailingSlash: String = {
      lazy val urlString = url.toString

      if (urlString.endsWith("/"))
        urlString
      else
        urlString + "/"
    }

    def /(node: String): URL =
      new URL(urlWithTrailingSlash + node)
  }

  implicit class WebJarOps(resource: JarResource) {
    def fonts(
        name: String = "",
        dir: String = "fonts",
        extensions: Seq[String] = Seq("eot", "svg", "ttf", "woff", "woff2")
      ): Seq[PageContent] = {
      val fontName = Option(name).filter(_.nonEmpty).getOrElse(FilenameUtils.getBaseName(resource.resourceName))
      extensions.map { ext =>
        Static(s"$dir/$fontName.$ext") from JarResource(resource.module, s"${resource.resourceName}.$ext")
      }
    }
  }

  def github(user: String, repo: String, version: String): GithubRepository =
    GithubRepository(user, repo, version)

  def resource(path: String): ResourceAsset =
    ResourceAsset(path)

  def scalaJsApplication(
      project: Project,
      launcher: Boolean = false,
      fastOpt: Boolean = false
    ): Def.Initialize[Seq[PageContent]] =
    Def.setting {
      val nameValue    = (name in project).value
      val targetValue  = (target in project).value
      val versionValue = (scalaVersion in project).value

      val output = targetValue / s"scala-${CrossVersion.binaryScalaVersion(versionValue)}"
      val files =
        Seq(
          if (fastOpt)
            Script from output / s"$nameValue-fastopt.js"
          else
            Script from output / s"$nameValue-opt.js"
        )

      if (launcher)
        files :+ (Script from output / s"$nameValue-launcher.js")
      else
        files
    }

  def scalaJsBundlerApplication(project: Project, fastOpt: Boolean = false): Def.Initialize[Seq[PageContent]] =
    Def.setting {
      val nameValue    = (name in project).value
      val targetValue  = (target in project).value
      val versionValue = (scalaVersion in project).value

      val output = targetValue / s"scala-${CrossVersion.binaryScalaVersion(versionValue)}" / "scalajs-bundler" / "main"
      val files =
        Seq(
          if (fastOpt)
            Script from output / s"$nameValue-fastopt-bundle.js"
          else
            Script from output / s"$nameValue-opt-bundle.js"
        )
      files
    }

  def scalaJsBundlerSourceMap(project: Project, fastOpt: Boolean = false): Def.Initialize[PageContent] =
    Def.setting {
      import sbt.{project => _, _}
      import sbt.Keys.{name, scalaVersion, target}

      val nameValue    = (name in project).value
      val targetValue  = (target in project).value
      val versionValue = (scalaVersion in project).value

      val output = targetValue / s"scala-${CrossVersion.binaryScalaVersion(versionValue)}" / "scalajs-bundler" / "main"
      val sourceMapName =
        if (fastOpt)
          s"$nameValue-fastopt-bundle.js.map"
        else
          s"$nameValue-opt-bundle.js.map"

      Static(s"scripts/$sourceMapName") from (output / sourceMapName)
    }

  def scalaJsApplicationSourceMap(project: Project, fastOpt: Boolean = false): Def.Initialize[PageContent] =
    Def.setting {
      import sbt.Keys.{name, scalaVersion, target}
      import sbt.{project => _, _}

      val nameValue    = (name in project).value
      val targetValue  = (target in project).value
      val versionValue = (scalaVersion in project).value

      val output = targetValue / s"scala-${CrossVersion.binaryScalaVersion(versionValue)}"
      val sourceMapName =
        if (fastOpt)
          s"$nameValue-fastopt.js.map"
        else
          s"$nameValue-opt.js.map"

      Static(s"scripts/$sourceMapName") from (output / sourceMapName)
    }

  object SJSShortcuts {
    def app(project: Project, fastOpt: Boolean = false) =
      Def.task {
        val compiled =
          if (fastOpt)
            (ScalaJS.fastOptJS in project).value
          else
            (ScalaJS.fullOptJS in project).value

        val sourceMap = compiled.metadata(ScalaJS.scalaJSSourceMap)

        Seq(
          Script.from(compiled.data),
          Static(sourceMap.getName).withMime("application/json").from(sourceMap)
        )
      }

    def bundlerApp(project: Project, fastOpt: Boolean = false) =
      Def.task {
        import scalajsbundler.BundlerFileType

        val compiled =
          if (fastOpt)
            (SJSBundler.webpack in ScalaJS.fastOptJS in project).value
          else
            (SJSBundler.webpack in ScalaJS.fullOptJS in project).value

        compiled.collect {
          case Attributed(script) if script.getName.endsWith(".js") => Script.from(script)

          case Attributed(sourceMap) if sourceMap.getName.endsWith(".map") =>
            Static(sourceMap.getName).withMime("application/json").from(sourceMap)

          case a @ Attributed(asset) if a.metadata(SJSBundler.BundlerFileTypeAttr) == BundlerFileType.Asset =>
            Static(asset.getName).from(asset)
        }.flatten
      }
  }
}

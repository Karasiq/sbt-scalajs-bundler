package com.karasiq.scalajsbundler.dsl

import java.net.URL

import org.apache.commons.io.FilenameUtils
import sbt._
import sbt.Keys._

import com.karasiq.scalajsbundler.ScalaJSBundler.{PageContent, ResourceAsset}

trait AssetShortcuts { self: BundlerDsl with BundlerImplicits ⇒
  def fontPackage(name: String, baseUrl: String, dir: String = "fonts", extensions: Seq[String] = Seq("eot", "svg", "ttf", "woff", "woff2")): Seq[PageContent] = {
    extensions.map { ext ⇒
      Static(s"$dir/$name.$ext") from new URL(s"$baseUrl.$ext")
    }
  }

  implicit class GithubRepositoryOps(gh: GithubRepository) {
    def fonts(name: String = "", dir: String = "fonts", extensions: Seq[String] = Seq("eot", "svg", "ttf", "woff", "woff2")): Seq[PageContent] = {
      val fontName = Option(name).filter(_.nonEmpty).getOrElse(FilenameUtils.getBaseName(gh.path.last))
      fontPackage(fontName, gh.url, dir, extensions)
    }
  }

  implicit class JarResourceOps(moduleId: ModuleID) {
    def /(resourceName: String): JarResource = {
      JarResource(moduleId, resourceName)
    }
  }

  implicit class WebJarOps(resource: JarResource) {
    def fonts(name: String = "", dir: String = "fonts", extensions: Seq[String] = Seq("eot", "svg", "ttf", "woff", "woff2")): Seq[PageContent] = {
      val fontName = Option(name).filter(_.nonEmpty).getOrElse(FilenameUtils.getBaseName(resource.resourceName))
      extensions.map { ext ⇒
        Static(s"$dir/$fontName.$ext") from JarResource(resource.module, s"${resource.resourceName}.$ext")
      }
    }
  }

  def github(user: String, repo: String, version: String): GithubRepository = {
    GithubRepository(user, repo, version)
  }

  def resource(path: String): ResourceAsset = {
    ResourceAsset(path)
  }

  def scalaJsApplication(project: Project, launcher: Boolean = false, fastOpt: Boolean = false): Def.Initialize[Seq[PageContent]] = Def.setting {
    val nameValue = (name in project).value
    val targetValue = (target in project).value
    val versionValue = (scalaVersion in project).value

    val output = targetValue / s"scala-${CrossVersion.binaryScalaVersion(versionValue)}"
    val files = Seq(
      if (fastOpt) Script from output / s"$nameValue-fastopt.js"
      else Script from output / s"$nameValue-opt.js"
    )

    if (launcher) files :+ (Script from output / s"$nameValue-launcher.js") else files
  }

  def scalaJsBundlerApplication(project: Project, fastOpt: Boolean = false): Def.Initialize[Seq[PageContent]] = Def.setting {
    val nameValue = (name in project).value
    val targetValue = (target in project).value
    val versionValue = (scalaVersion in project).value

    val output = targetValue / s"scala-${CrossVersion.binaryScalaVersion(versionValue)}" / "scalajs-bundler" / "main"
    val files = Seq(
      if (fastOpt) Script from output / s"$nameValue-fastopt-bundle.js"
      else Script from output / s"$nameValue-opt-bundle.js"
    )
    files
  }
}

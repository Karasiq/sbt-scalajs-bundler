package com.karasiq.scalajsbundler.dsl

import java.net.URL

import com.karasiq.scalajsbundler.ScalaJSBundler.{PageContent, ResourceAsset}
import org.apache.commons.io.FilenameUtils
import org.scalajs.sbtplugin.JarJSModuleID
import sbt.Keys._
import sbt._

trait AssetShortcuts { self: BundlerDsl ⇒
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

  implicit class WebJarOps(moduleId: JarJSModuleID) {
    def fonts(name: String = "", dir: String = "fonts", extensions: Seq[String] = Seq("eot", "svg", "ttf", "woff", "woff2")): Seq[PageContent] = {
      val fontName = Option(name).filter(_.nonEmpty).getOrElse(FilenameUtils.getBaseName(moduleId.jsDep.resourceName))
      extensions.map { ext ⇒
        Static(s"$dir/$fontName.$ext") from JarJSModuleID(moduleId.module, s"${moduleId.jsDep.resourceName}.$ext")
      }
    }
  }

  def github(user: String, repo: String, version: String): GithubRepository = {
    GithubRepository(user, repo, version)
  }

  def resource(path: String): ResourceAsset = {
    ResourceAsset(path)
  }

  def scalaJsApplication(project: Project, launcher: Boolean = true, fastOpt: Boolean = false): Def.Initialize[Seq[PageContent]] = {
    (name in project, target in project, scalaVersion in project) {
      case (name, target, version) ⇒
        val output = target / s"scala-${CrossVersion.binaryScalaVersion(version)}"
        val files = Seq(
          if (fastOpt) Script from output / s"$name-fastopt.js"
          else Script from output / s"$name-opt.js"
        )

        if (launcher) files :+ (Script from output / s"$name-launcher.js") else files
    }
  }
}

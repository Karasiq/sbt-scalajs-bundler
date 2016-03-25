package com.karasiq.scalajsbundler.dsl

import java.net.URL

import com.karasiq.scalajsbundler.ScalaJSBundler.{PageContent, ResourceAsset}
import sbt.Keys._
import sbt._

trait AssetShortcuts { self: BundlerDsl ⇒
  def fontPackage(name: String, baseUrl: String, dir: String = "fonts", extensions: Seq[String] = Seq("eot", "svg", "ttf", "woff", "woff2")): Seq[PageContent] = {
    extensions.map { ext ⇒
      Static(s"$dir/$name.$ext") from new URL(s"$baseUrl.$ext")
    }
  }

  def github(user: String, repo: String, version: String): GithubRepository = {
    GithubRepository(user, repo, version)
  }

  def resource(path: String): ResourceAsset = {
    ResourceAsset(path)
  }

  def scalaJsApplication(project: Project, launcher: Boolean = true, fastOpt: Boolean = false) = {
    (name in project, target in project, scalaVersion in project) {
      case (name, target, version) ⇒
        val output = target / s"scala-${CrossVersion.binaryScalaVersion(version)}"
        var files = Vector.empty[PageContent]
        if (fastOpt) files :+= Script from output / s"$name-fastopt.js"
        else files :+= Script from output / s"$name-opt.js"

        if (launcher) files :+= Script from output / s"$name-launcher.js"
        files
    }
  }
}

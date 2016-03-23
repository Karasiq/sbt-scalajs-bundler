package com.karasiq.scalajsbundler.dsl

import java.net.URL

import com.karasiq.scalajsbundler.ScalaJSBundler.{PageContent, ResourceAsset}

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
}

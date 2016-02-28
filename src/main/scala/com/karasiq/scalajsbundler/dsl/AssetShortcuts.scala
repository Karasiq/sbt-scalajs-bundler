package com.karasiq.scalajsbundler.dsl

import java.net.URL

import com.karasiq.scalajsbundler.ScalaJSBundler.PageContent

trait AssetShortcuts { self: BundlerDsl ⇒
  def fontPackage(name: String, baseUrl: String, dir: String = "fonts", extensions: Seq[String] = Seq("eot", "svg", "ttf", "woff", "woff2")): Seq[PageContent] = {
    extensions.map { ext ⇒
      Static(s"$dir/$name.$ext") from new URL(s"$baseUrl.$ext")
    }
  }

  case class GithubRepository(user: String, repo: String, version: String, path: Seq[String] = Nil) {
    def %(file: String): String = {
      s"https://raw.githubusercontent.com/$user/$repo/v$version/${if (path.nonEmpty) path.mkString("", "/", "/") else ""}$file"
    }

    def /(rt: String): GithubRepository = {
      copy(path = path :+ rt)
    }
  }

  def github(user: String, repo: String, version: String): GithubRepository = {
    GithubRepository(user, repo, version)
  }
}

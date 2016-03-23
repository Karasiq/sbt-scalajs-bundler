package com.karasiq.scalajsbundler.dsl

case class GithubRepository(user: String, repo: String, version: String, path: Seq[String] = Nil) {
  def url: String = s"https://raw.githubusercontent.com/$user/$repo/$version/${if (path.nonEmpty) path.mkString("", "/", "") else ""}"

  def %(file: String): String = {
    /(file).url
  }

  def /(rt: String): GithubRepository = {
    copy(path = path :+ rt)
  }
}

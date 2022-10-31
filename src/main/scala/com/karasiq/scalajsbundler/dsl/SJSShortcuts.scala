package com.karasiq.scalajsbundler.dsl

import com.karasiq.scalajsbundler.ScalaJSBundler
import sbt.Project

trait SJSShortcuts {
  def app(project: Project, fastOpt: Boolean = false): Seq[ScalaJSBundler.PageTypedContent]
  def bundlerApp(project: Project, fastOpt: Boolean = false): Seq[ScalaJSBundler.PageContent]
}

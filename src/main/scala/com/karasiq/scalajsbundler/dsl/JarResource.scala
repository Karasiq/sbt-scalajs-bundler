package com.karasiq.scalajsbundler.dsl

import sbt.ModuleID

case class JarResource(module: ModuleID, resourceName: String)
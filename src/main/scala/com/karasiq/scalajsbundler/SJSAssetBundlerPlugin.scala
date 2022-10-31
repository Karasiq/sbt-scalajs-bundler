package com.karasiq.scalajsbundler

import com.karasiq.scalajsbundler

import java.io.IOException
import java.nio.file.{Path, _}
import java.nio.file.attribute.BasicFileAttributes
import scala.collection.mutable.ListBuffer
import sbt.{Def, _}
import sbt.Keys._
import sbt.plugins.JvmPlugin
import com.karasiq.scalajsbundler.ScalaJSBundler.Bundle
import com.karasiq.scalajsbundler.compilers.AssetCompilers
import com.karasiq.scalajsbundler.dsl.SJSShortcuts

object SJSAssetBundlerPlugin extends AutoPlugin {
  private def clearDirectory(destDir: Path): Unit =
    if (Files.isSymbolicLink(destDir) && Files.isRegularFile(destDir)) {
      Files.delete(destDir)
    } else if (Files.isDirectory(destDir)) {
      Files.walkFileTree(
        destDir,
        new SimpleFileVisitor[Path] {
          override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
            Files.delete(file)
            FileVisitResult.CONTINUE
          }

          override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
            Files.delete(dir)
            FileVisitResult.CONTINUE
          }
        }
      )
    }

  private def fileList(destDir: Path): Seq[File] = {
    val buffer = new ListBuffer[File]()
    Files.walkFileTree(
      destDir,
      new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          buffer += file.toFile
          FileVisitResult.CONTINUE
        }
      }
    )

    buffer.result()
  }

  object autoImport {
    val scalaJsBundlerAssets = taskKey[Seq[Bundle]]("Scala.js bundler resources.")
    val scalaJsBundlerDest   = settingKey[File]("Scala.js bundler output directory.")
    val scalaJsBundlerInline =
      settingKey[Boolean]("Scala.js bundler inline setting. All scripts and styles will be inlined in HTML if enabled.")
    val scalaJsBundlerCompilers = settingKey[AssetCompilers]("Scala.js asset compilers.")
    val scalaJsBundlerCompile   = taskKey[Seq[File]]("Compiles Scala.js bundles.")

    val SJSApps = scalajsbundler.dsl.SJSShortcuts
    val SJSAssetCompilers = AssetCompilers

    lazy val baseSJSAssetBundlerSettings: Seq[Def.Setting[_]] =
      Seq(
        scalaJsBundlerAssets    := Nil,
        scalaJsBundlerDest      := resourceManaged.value / "webapp",
        scalaJsBundlerInline    := false,
        scalaJsBundlerCompilers := AssetCompilers.default,
        scalaJsBundlerCompile := {
          streams.value.log.info("Compiling Scala.js assets")
          clearDirectory(scalaJsBundlerDest.value.toPath)
          val compiler = new ScalaJSBundleCompiler
          scalaJsBundlerAssets.value.foreach { case Bundle(page, contents @ _*) =>
            compiler.createHtml(
              scalaJsBundlerCompilers.value,
              scalaJsBundlerDest.value.toString,
              page,
              contents.flatten,
              scalaJsBundlerInline.value
            )
          }
          fileList(scalaJsBundlerDest.value.toPath)
        },
        managedResources ++= scalaJsBundlerCompile.value
      )
  }

  import autoImport._

  override def requires: Plugins =
    JvmPlugin

  override val projectSettings: Seq[Def.Setting[_]] = inConfig(Compile)(baseSJSAssetBundlerSettings)
}

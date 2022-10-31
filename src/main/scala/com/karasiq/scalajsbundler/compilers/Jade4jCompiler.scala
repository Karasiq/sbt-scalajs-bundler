package com.karasiq.scalajsbundler.compilers

import java.io.InputStreamReader

import com.karasiq.scalajsbundler.ScalaJSBundler.{FileAsset, PageTypedContent}
import de.neuland.jade4j.Jade4J
import org.apache.commons.io.IOUtils

import scala.collection.JavaConversions._
import scala.util.control.Exception

class Jade4jCompiler extends AssetCompiler {
  override def compile(contents: Seq[PageTypedContent]): String = {
    val compiled =
      contents.map(_.asset match {
        case FileAsset(file) => Jade4J.render(file, Map.empty[String, AnyRef], false)

        case a =>
          val reader = new InputStreamReader(a.content(), "UTF-8")
          Exception.allCatch.andFinally(IOUtils.closeQuietly(reader)) {
            Jade4J.render(reader, "input.jade", Map.empty[String, AnyRef], false)
          }
      })

    HtmlConcatCompiler.concat(compiled)
  }
}

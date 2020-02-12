val scalaJSVersion = sys.env.getOrElse("SCALAJS_VERSION", "0.6.32")

organization := "com.github.karasiq"

name := "sbt-scalajs-bundler" // (if (scalaJSVersion.startsWith("1.")) "sbt-scalajs-bundler" else "sbt-scalajs-bundler-sjs06")

version := "1.2.2"

isSnapshot := version.value.endsWith("SNAPSHOT")

crossSbtVersions := Seq("0.13.16", "1.1.0")

sbtPlugin := true

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "org.webjars" % "bootstrap" % "3.3.6" % "test",
  "org.webjars" % "jquery" % "2.1.3" % "test",
  "commons-io" % "commons-io" % "2.4",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.jsoup" % "jsoup" % "1.8.3",
  "com.google.javascript" % "closure-compiler" % "v20190513" % "provided",
  "com.yahoo.platform.yui" % "yuicompressor" % "2.4.8",
  "com.googlecode.htmlcompressor" % "htmlcompressor" % "1.5.2",
  "de.neuland-bfi" % "jade4j" % "1.1.4" % "provided",
  "com.github.sommeri" % "less4j" % "1.15.4" % "provided",
  "com.sun.activation" % "javax.activation" % "1.2.0"
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ ⇒ false }

licenses := Seq("Apache License, Version 2.0" → url("http://opensource.org/licenses/Apache-2.0"))

homepage := Some(url("https://github.com/Karasiq/" + name.value))

pomExtra := <scm>
  <url>git@github.com:Karasiq/{name.value}.git</url>
  <connection>scm:git:git@github.com:Karasiq/{name.value}.git</connection>
</scm>
  <developers>
    <developer>
      <id>karasiq</id>
      <name>Piston Karasiq</name>
      <url>https://github.com/Karasiq</url>
    </developer>
  </developers>

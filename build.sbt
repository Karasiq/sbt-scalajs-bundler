lazy val `sbt-scalajs-bundler` =
  project.in(file("."))
    .settings(projectSettings, publishSettings)

// Settings
lazy val projectSettings =
  Seq(
    organization := "com.github.karasiq",
    name         :=
      (if (Deps.isScalaJs06)
         "sbt-scalajs-bundler-sjs06"
       else
         "sbt-scalajs-bundler"),
    crossSbtVersions := (if (Deps.isScalaJs06)
                           Seq("0.13.16", sbtVersion.value)
                         else
                           Seq(sbtVersion.value)),
    sbtPlugin :=
      true,
    libraryDependencies ++= Seq(
      "org.scalatest"                %% "scalatest"        % "3.0.4"     % Test,
      "org.webjars"                   % "bootstrap"        % "3.3.6"     % Test,
      "org.webjars"                   % "jquery"           % "2.1.3"     % Test,
      "commons-io"                    % "commons-io"       % "2.4",
      "org.scalaj"                   %% "scalaj-http"      % "2.3.0",
      "org.jsoup"                     % "jsoup"            % "1.8.3",
      "com.google.javascript"         % "closure-compiler" % "v20190513" % Provided,
      "com.yahoo.platform.yui"        % "yuicompressor"    % "2.4.8",
      "com.googlecode.htmlcompressor" % "htmlcompressor"   % "1.5.2",
      "de.neuland-bfi"                % "jade4j"           % "1.1.4"     % Provided,
      "com.github.sommeri"            % "less4j"           % "1.15.4"    % Provided
    ),
    addSbtPlugin(Deps.ScalaJS        % Provided),
    addSbtPlugin(Deps.ScalaJSBundler % Provided)
  )

lazy val publishSettings =
  Seq(
    sonatypeSessionName     := s"${name.value} v${version.value}",
    publishConfiguration    := publishConfiguration.value.withOverwrite(true),
    publishTo               := sonatypePublishToBundle.value,
    publishMavenStyle       := true,
    publishArtifact in Test := false,
    pomIncludeRepository    := { _ => false },
    licenses                := Seq("Apache License, Version 2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
    homepage                := Some(url("https://github.com/Karasiq/" + name.value)),
    pomExtra := <scm>
      <url>git@github.com:Karasiq/
        {name.value}
        .git</url>
      <connection>scm:git:git@github.com:Karasiq/
        {name.value}
        .git</connection>
    </scm>
      <developers>
        <developer>
          <id>karasiq</id>
          <name>Piston Karasiq</name>
          <url>https://github.com/Karasiq</url>
        </developer>
      </developers>
  )

lazy val Deps =
  new {
    lazy val ScalaJSVersion = sys.props.getOrElse("SCALAJS_VERSION", "1.14.0")

    def isScalaJs06: Boolean =
      ScalaJSVersion.startsWith("0.6.")

    lazy val ScalaJS = "org.scala-js" % "sbt-scalajs" % ScalaJSVersion

    lazy val ScalaJSBundler =
      if (isScalaJs06)
        "ch.epfl.scala" % "sbt-scalajs-bundler-sjs06" % "0.18.0"
      else
        "ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1"

  }

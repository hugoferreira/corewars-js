enablePlugins(ScalaJSPlugin)

name := "core-wars-js"

version := "0.1"

scalaVersion := "2.11.8"

persistLauncher := true

persistLauncher in Test := false

resolvers ++= Seq(
  "bintray-alexander_myltsev" at "http://dl.bintray.com/content/alexander-myltsev/maven"
)

libraryDependencies ++= Seq(
  "org.scala-js"    %%% "scalajs-dom"   % "0.9.0",
  "org.parboiled"   %%% "parboiled"     % "2.0.1",
  "org.scala-lang"    % "scala-reflect" % "2.11.8"
  /*,"com.lihaoyi"     %%% "fastparse"     % "0.1.5"*/)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions")

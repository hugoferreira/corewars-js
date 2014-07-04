name := "core-wars-js"

version := "0.1"

scalaVersion := "2.11.1"

scalaJSSettings

resolvers ++= Seq(
  "snapshots"           at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"            at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "bintray-alexander_myltsev" at "http://dl.bintray.com/content/alexander-myltsev/maven"
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules.scalajs" %%% "scalajs-dom"   % "0.6",
  "name.myltsev" 		   %%% "parboiled"     % "2.0.0",
  "org.scala-lang" 		     % "scala-reflect" % "2.11.1")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions")

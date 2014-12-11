name := "core-wars-js"

version := "0.1"

scalaVersion := "2.11.4"

scalaJSSettings

resolvers ++= Seq(
  "snapshots"                 at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"                  at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository"       at "http://repo.typesafe.com/typesafe/releases/",
  "bintray-alexander_myltsev" at "http://dl.bintray.com/content/alexander-myltsev/maven"
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules.scalajs" %%% "scalajs-dom"   % "0.6",
  "org.parboiled" 		             %%% "parboiled"     % "2.0.1",
  "org.scala-lang" 		               % "scala-reflect" % "2.11.4")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions")

organization := "ch.hepia"
val neotypesV = "0.4.0"

name := "parser"

version := "0.0.1"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.5",
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",
  "com.dimafeng" %% "neotypes" % neotypesV
)

ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "SampleUBAM",
    idePackagePrefix := Some("org.ydnawarehouse")
  )

libraryDependencies += "com.github.samtools" % "htsjdk" % "4.1.3"

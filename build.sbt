ThisBuild / version := "0.0.3"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "SampleUBAM",
    idePackagePrefix := Some("org.ydnawarehouse"),

    // Assembly settings
    mainClass := Some("org.ydnawarehouse.SampleUBAM"),
    test in assembly := {}, // Disable tests during assembly

    // Merge strategy for handling potential resource conflicts
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _ @_*) => MergeStrategy.discard
      case _                           => MergeStrategy.first
    }
  )

libraryDependencies += "com.github.samtools" % "htsjdk" % "4.1.3"
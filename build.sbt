name := "linoleum"

organization := "linoleum"

version := "1.0"

mainClass in (Compile, packageBin) := Some("linoleum.Desktop")

mainClass in (Compile, run) := Some("linoleum.Desktop")

libraryDependencies := Seq("org.apache.ivy" % "ivy" % "2.4.0", "org.swinglabs" % "pdf-renderer" % "1.0.5", "javax.media" % "jmf" % "2.1.1e", "java3d" % "j3d-core" % "1.3.1", "java3d" % "j3d-core-utils" % "1.3.1", "linoleum" % "application" % "1.0", "linoleum" % "jlfgr" % "1_0")

crossPaths := false

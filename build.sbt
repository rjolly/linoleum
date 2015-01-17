name := "linoleum"

organization := "linoleum"

version := "1.0"

mainClass in (Compile, packageBin) := Some("linoleum.Desktop")

mainClass in (Compile, run) := Some("linoleum.Desktop")

libraryDependencies := Seq("org.apache.ivy" % "ivy" % "2.4.0", "linoleum" % "application" % "1.0", "linoleum" % "jlfgr" % "1_0")

crossPaths := false

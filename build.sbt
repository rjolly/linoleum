name := "linoleum"

organization := "net.java.linoleum"

version := "1.0"

mainClass in (Compile, packageBin) := Some("linoleum.Desktop")

mainClass in (Compile, run) := Some("linoleum.Desktop")

libraryDependencies := Seq("org.apache.ivy" % "ivy" % "2.4.0", "com.github.rjolly" % "pdf-renderer" % "140", "javax.media" % "jmf" % "2.1.1e", "java3d" % "j3d-core" % "1.3.1", "java3d" % "j3d-core-utils" % "1.3.1", "net.java.linoleum" % "application" % "1.1", "net.java.linoleum" % "jlfgr" % "1_0")

autoScalaLibrary := false

crossPaths := false

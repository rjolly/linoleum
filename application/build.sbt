name := "application"

organization := "linoleum"

version := "1.0"

javaSource in Compile := baseDirectory.value / "src"

resourceDirectory in Compile := baseDirectory.value / "src"

crossPaths := false

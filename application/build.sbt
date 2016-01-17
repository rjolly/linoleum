name := "application"

organization := "net.java.linoleum"

description := "Java desktop environment and software distribution"

licenses := Seq( "LGPL" -> url( "http://www.gnu.org/licenses/lgpl.txt" ))

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

pomExtra :=
  <url>http://linoleum.java.net/</url>
  <scm>
    <url>git@github.com:rjolly/linoleum.git</url>
    <connection>scm:git:git@github.com:rjolly/linoleum.git</connection>
  </scm>
  <developers>
    <developer>
      <id>rjolly</id>
      <name>Raphael Jolly</name>
      <url>http://github.com/rjolly</url>
    </developer>
  </developers>

version := "1.1"

autoScalaLibrary := false

crossPaths := false

name := "jlfgr"

organization := "net.java.linoleum"

description := "Java Look and Feel Graphics Repository"

licenses := Seq( "LICENSE FOR SOFTWARE GRAPHICS ARTWORK" -> url( "http://download.oracle.com/otn-pub/java/licenses/Software_Icon_License__943_2012.pdf" ))

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

pomExtra :=
  <url>http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-client-419417.html</url>
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

version := "1_0"

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

autoScalaLibrary := false

crossPaths := false

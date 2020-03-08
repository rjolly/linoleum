
Required software:

- jdk ( http://www.oracle.com/technetwork/java/index.html )

Optional software:

- jmf 2.1.1e ( http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-client-419417.html )
- java3d 1.5.1 ( same as above )
- jai 1.1.2_01 ( same as above )


To run linoleum, add bin to your path, give bin/linoleum execution privilege (unix), then:
  linoleum


Once in linoleum, to execute a shell:
  File->Open
  double-click on "ScriptShell"


To install an application (or any Ivy module), in the script shell:
  install("org#module;version");


Alternatively:
  open "Packages"
  enter the (org, module, version) triplet
  click "Install"


To build linoleum from itself, first install jgit, then clone the repository, change dir to it, and run "build-all". In the script shell:
  install("org.eclipse.jgit#org.eclipse.jgit;3.4.0.201406110918-r");
  clone("https://github.com/rjolly/linoleum.git");
  cd("linoleum")
  load("build-all.js");


To publish an application:

- add net.java.linoleum#application;1.6 to your project's dependencies
- extend linoleum.application.Frame
- make it available to the service loader in META-INF/services/javax.swing.JInternalFrame
- publish your artifact in maven central
- let me know so that I put it in the list (below)


To use linoleum as your desktop environment in Linux:
  wget http://raphael.jolly.free.fr/linoleum/linoleum-1.6-buster.deb
  sudo dpkg -i linoleum-1.6-buster.deb

You might have to enable TCP connections to your X server. In /etc/gdm3/daemon.conf:

[security]
DisallowTCP = false

Then reload the display manager:
  sudo systemctl reload gdm3


List of applications

  com.github.rjolly#flying-saucer;9.1.20	XML/XHTML+MathML+SVG and CSS 2.1 browser in pure Java
  com.github.rjolly#mappanel;1.0		MapPanel renders OpenstreetMaps using just basic java
  net.java.linoleum#console;1.6			JConsole
  net.java.linoleum#j3d;1.2			3D Object Loader (requires java3d)
  net.java.linoleum#jcterm;0.0.11		SSH2 Terminal Emulator in Pure Java
  net.java.linoleum#media;1.6			Media Player (requires jmf)
  net.java.linoleum#pdfview;1.6			PDF viewer (may require jai in some cases)
  net.sourceforge.jscl-meditor#meditor;5.3.3	Java symbolic computing library and mathematical editor


Useful libraries

  com.googlecode.java-diff-utils#diffutils;1.3.0	The DiffUtils library for computing diffs in Java
  commons-io#commons-io;1.3.1			The Apache Commons IO library
  mstor#mstor;0.9.9				A JavaMail provider for persistent email storage
  org.bouncycastle#bcpg-jdk15on;1.64		The Bouncy Castle Java API for handling the OpenPGP protocol
  org.ghost4j#ghost4j;1.0.1			Ghost4J binds the Ghostscript C API to bring Ghostscript power to the Java world
  net.sourceforge.jscl-meditor#txt2xhtml;2.0	Text to XHTML converter


Scala

  ch.epfl.lamp#dotty-compiler_0.22;0.22.0-RC1	Dotty compiler (bootstrapped)
  com.lihaoyi#os-lib_2.13;0.6.3			Scala interface to common OS filesystem and subprocess APIs


Scala 2.11

  org.scala-lang#scala-compiler;2.11.0-M6	Compiler for the Scala Programming Language
  com.github.rjolly#jarlister_2.11;1.1		A tool that lists a jar's classes into its manifest


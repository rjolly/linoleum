
Required software:

- jdk 1.7 ( http://www.oracle.com/technetwork/java/index.html )
- jmf 2.1.1e ( http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-client-419417.html )
- java3d 1.5.1 ( same as above )

Optional software:

- netbeans 7.4 ( https://netbeans.org/ )
- ivybeans 1.2-nb71 ( https://code.google.com/p/ivybeans/ )


To run linoleum, add dist/bin to your path, give dist/bin/linoleum execution privilege (unix), then:
  linoleum


Once in linoleum, to execute a shell:
  File->Open
  double-click on "ScriptShell"


To install an application (or any Ivy module), in the script shell:
  install("org#module;version");


To build linoleum from itself, first clone the repository by external means (for now), then in the script shell:
  cd("/path/to/linoleum");
  load("build.js")


To publish an application:

- add net.java.linoleum#application;1.1 to your project's dependencies
- extend linoleum.application.Frame
- make it available to the service loader in META-INF/services/javax.swing.JInternalFrame
- publish your artifact in maven central
- let me know so that I put it in the list (below)


To use linoleum as your desktop environment in Linux:
  sudo cp linoleum.desktop /usr/share/xsessions/


List of applications

  net.java.linoleum#jcterm;0.0.11	SSH2 Terminal Emulator in Pure Java
  net.java.linoleum#pdfview;1.1		PDF viewer.


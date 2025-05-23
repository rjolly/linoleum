
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
  install("org.eclipse.jgit#org.eclipse.jgit;7.2.1.202505142326-r");
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
  wget http://raphael.jolly.free.fr/linoleum/linoleum-1.6-bookworm.deb
  sudo apt-get install openjdk-17-jdk openjdk-17-doc ivy libcommons-codec-java libmail-java libjava3d-java
  sudo ln -s /usr/share/doc/openjdk-17-jdk /usr/lib/jvm/java-17-openjdk-amd64/docs
  sudo dpkg -i linoleum-1.6-bookworm.deb

You might have to enable TCP connections to your X server. In /etc/gdm3/daemon.conf:

[security]
DisallowTCP = false

Then reload the display manager:
  sudo systemctl reload gdm3


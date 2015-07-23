
Software needed:

- jdk 1.7 ( http://www.oracle.com/technetwork/java/index.html )
- jmf 2.1.1e ( http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-client-419417.html )
- java3d 1.5.1 ( same as above )
- netbeans 7.4 ( https://netbeans.org/ )
- ivybeans 1.2-nb71 ( https://code.google.com/p/ivybeans/ )


To build linoleum, first build "application" and "jlfgr", then "linoleum"


To run linoleum, add dist/bin to your path, give dist/bin/linoleum execution privilege (unix), then:
  linoleum


Once in linoleum, to execute a shell:
  File->Open
  double-click on "ScriptShell"


To install any Ivy module, in the script shell:
  install("org#module;version"); // For instance:
  install("linoleum#jcterm;0.0.11");


(This requires to first clone/build the project at https://github.com/rjolly/jcterm for the moment). A "JCTerm" item should show up in the Applications. If not, you can troubleshoot with:
  Console


To build linoleum from itself, first clone the repository by external means (for now), then in the script shell:
  cd("/path/to/linoleum");
  mkdirs("build/classes");
  javac("src/main/java", "build/classes");


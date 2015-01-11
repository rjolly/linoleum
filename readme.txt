
Prerequisites:

  sudo apt-get install ant ant-contrib ivy
  mkdir -p ~/.ant/lib
  ln -s /usr/share/java/ant-contrib.jar ~/.ant/lib/
  ln -s /usr/share/java/ivy.jar ~/.ant/lib/


To build linoleum:
  cd application
  ant jar
  cd ..
  ant jar


To run linoleum, add the bin directory to your path, give bin/linoleum execution privilege (unix), then:
  linoleum


Once in linoleum, to execute a shell:
  File->Open
  double-click on "ScriptShell"


To install any Ivy module, in the script shell:
  install("org#module;version"); // For instance:
  install("linoleum#jcterm;0.0.11");


(This requires to first clone/build the project at https://github.com/rjolly/jcterm for the moment). A "JCTerm" item should show up in the Applications. If not, you can troubleshoot with:
  View->Console


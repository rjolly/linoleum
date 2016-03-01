mkdir("build");
mkdir("build/classes");
mkdir("build/sources");
mkdir("build/javadoc");

javac("src/main/java", "build/classes");
copy("src/main/java", "build/sources");
javadoc("src/main/java", "build/javadoc");

mkdir("dist");
var name = "application";
jar("dist/" + name + ".jar", "build/classes");
jar("dist/" + name + "-source.jar", "build/sources");
jar("dist/" + name + "-javadoc.jar", "build/javadoc");
cp("pom.xml", "dist/" + name + ".pom")

publish("dist")

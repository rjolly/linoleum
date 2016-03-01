mkdir("build");
mkdir("build/classes");
mkdir("build/sources");
mkdir("build/javadoc");

javac("src/main/java", "build/classes");
copy("src/main/resources", "build/classes");
copy("src/main/java", "build/sources");
copy("src/main/resources", "build/sources");
remove("build/sources/META-INF");
javadoc("src/main/java", "build/javadoc");

mkdir("dist");
var name = "linoleum";
jar("dist/" + name + ".jar", "build/classes", ".*", "manifest.mf");
jar("dist/" + name + "-source.jar", "build/sources");
jar("dist/" + name + "-javadoc.jar", "build/javadoc");

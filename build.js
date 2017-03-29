mkdir("build");
mkdir("build/classes");

javac("src/main/java", "build/classes");
copy("src/main/resources", "build/classes");

mkdir("dist");
var name = "linoleum";
jar("dist/" + name + ".jar", "build/classes", ".*", "manifest.mf");

publish("dist")

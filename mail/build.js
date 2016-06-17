mkdir("build");
mkdir("build/classes");

javac("src", "build/classes");
copy("res", "build/classes");

mkdir("dist");
var name = "mail";
jar("dist/" + name + ".jar", "build/classes");

publish("dist")

mkdir("dist");
var name = "jlfgr";
jar("dist/" + name + ".jar", "res", ".*", "manifest.mf");
cp("pom.xml", "dist/" + name + ".pom")

publish("dist")

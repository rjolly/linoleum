cd("application")
clean("build/classes")
load("build.js")

cd("../html")
clean("build/classes")
load("build.js")

cd("../mail")
clean("build/classes")
load("build.js")

cd("../notepad")
clean("build/classes")
load("build.js")

cd("../pkg")
clean("build/classes")
load("build.js")

cd("..")
clean("build/classes")
load("build.js")

var version = "1.3.1";
install("net.java.linoleum#linoleum;" + version)

mkdir("dist/bin")
copy("bin", "dist/bin")
cp("init.js", "dist/init.js")

mkdir("dist/lib")
copy(new File("lib").getAbsolutePath(), "dist/lib")

rm("dist/lib/linoleum-" + version + ".jar")
jar("../linoleum.zip", "dist");

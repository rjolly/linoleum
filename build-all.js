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

var version = "1.4";
install("net.java.linoleum#linoleum;" + version)
rm(new File(new File("lib"), "linoleum-" + version + ".jar"))

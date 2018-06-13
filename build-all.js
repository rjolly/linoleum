cd("application")
load("build.js")

cd("../html")
load("build.js")

cd("../mail")
load("build.js")

cd("../calendar")
load("build.js")

cd("../notepad")
load("build.js")

cd("../pkg")
load("build.js")

cd("..")
load("build.js")

var version = "1.6";
install("net.java.linoleum#linoleum;" + version)
rm(new File(new File("lib"), "linoleum-" + version + ".jar"))

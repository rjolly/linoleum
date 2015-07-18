function install(pkg) {
    frames[0].getPackageManager().install([pkg]);
}

function installed() {
    return frames[0].getPackageManager().listFiles();
}

// adapted from https://weblogs.java.net/blog/forax/archive/2006/09/using_jrunscrip.html

function javac(srcDir, destDir) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    ToolProvider = javax.tools.ToolProvider;
    StandardLocation = javax.tools.StandardLocation;
    Arrays = java.util.Arrays;

    compiler = ToolProvider.getSystemJavaCompiler()
    fileManager = compiler.getStandardFileManager(null, null, null)
    fileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(installed()))
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList([pathToFile(destDir)]))

    compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(fileset(srcDir, ".*\.java")))
    task = compiler.getTask(null, fileManager, null, null, null, compilationUnit)
    task.call()

    fileManager.close()
}

function fileset(path, pattern) {
    var set = new Array()
    function callback(file) {
	set.push(file)
    }
    find(path, pattern, callback)
    return set
}

function open(name) {
    Class = java.lang.Class;
    URLClassLoader = java.net.URLClassLoader;

    f = Class.forName(name, true, URLClassLoader([pathToFile(".").toURI().toURL()])).newInstance().open(null)
    frame.getDesktopPane().add(f)
    f.setVisible(true);
}

function pwd() {
    println(curDir);
}

function cd(target) {
    if (target == undefined) {
	target = sysProps["user.home"];
    }
    if (!(target instanceof File)) {
	target = pathToFile(target);
    }
    if (target.exists() && target.isDirectory()) {
	curDir = target.getCanonicalFile();
    } else {
	println(target + " is not a directory");
    }
}

function install(pkg, conf) {
    if (conf == undefined) {
	conf = "default";
    }
    frame.getDesktop().getPackageManager().install(pkg, conf);
}

function installed() {
    return frame.getDesktop().getPackageManager().getLib().listFiles();
}

// adapted from https://weblogs.java.net/blog/forax/archive/2006/09/using_jrunscrip.html

function javac(srcDir, destDir) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    Packages.linoleum.Tools.instance.compile(fileset(srcDir, ".*\.java"), installed(), pathToFile(destDir), ["-source", "1.7", "-target", "1.7"]);
}

function jar(dest, dir, pattern) {
    if (dir == undefined) {
	dir = ".";
    }
    Packages.linoleum.Tools.instance.jar(pathToFile(dir), fileset(dir, pattern), pathToFile(dest));
}

function clean(dir) {
    if (dir == undefined) {
	dir = ".";
    }
    fileset(dir, ".*\.class").forEach(rm);
}

function fileset(path, pattern) {
    var set = new Array()
    function callback(file) {
	set.push(file)
    }
    find(path, pattern, callback)
    return set
}

function run(name) {
    var array = new Array(arguments.length - 1);
    for (var i = 0; i < array.length; i++) {
	array[i] = arguments[i+1];
    }
    Packages.linoleum.Tools.instance.run(name, curDir, array);
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

function open(name) {
    frame.getApplicationManager().open(pathToFile(name).toURI());
}

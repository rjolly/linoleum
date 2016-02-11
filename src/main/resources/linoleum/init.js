function install(pkg, conf) {
    if (conf == undefined) {
	conf = "default";
    }
    Packages.linoleum.PackageManager.instance.install(pkg, conf);
}

function installed() {
    return Packages.linoleum.PackageManager.instance.installed();
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

function javadoc(srcDir, destDir) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    files = fileset(srcDir, ".*\.java");
    dir = pathToFile(destDir);
    Packages.com.sun.tools.javadoc.Main.execute(["-d", dir].concat(files));
}

function copy(src, dest, pattern) {
    Packages.linoleum.Tools.instance.copy(pathToFile(src), fileset(src, pattern), pathToFile(dest));
}

function jar(dest, dir, pattern, manifest) {
    if (dir == undefined) {
	dir = ".";
    }
    if (manifest == undefined) {
	Packages.linoleum.Tools.instance.jar(pathToFile(dir), fileset(dir, pattern), pathToFile(dest));
    } else {
	Packages.linoleum.Tools.instance.jar(pathToFile(dir), fileset(dir, pattern), pathToFile(dest), pathToFile(manifest));
    }
}

function clean(dir) {
    if (dir == undefined) {
	dir = ".";
    }
    find(dir, ".*\.class", rm);
    finddir(dir, rmdir);
    rmdir(dir);
}

function cat(obj, pattern) {
    if (obj instanceof File && obj.isDirectory()) {
        ls(obj);
        return;
    }

    var inp = null;
    if (!(obj instanceof Reader)) {
        inp = inStream(obj);
        obj = new BufferedReader(new InputStreamReader(inp));
    }
    var line;
    if (pattern) {
        var count = 1;
        while ((line=obj.readLine()) != null) {
            if (line.match(pattern)) {
                println(count + "\t: " + line);
            }
            count++;
        }
    } else {
        while ((line=obj.readLine()) != null) {
            println(line);
        }
    }
    obj.close();
}

function grep(pattern, dir, files) {
    if (dir == undefined) {
	dir = ".";
    }
    function callback(file) {
	println(java.util.regex.Pattern.compile("\\\\").matcher(file.getCanonicalPath()).replaceAll("/") + ":");
	cat(file, pattern);
    }
    dir = pathToFile(dir);
    if (dir.isDirectory()) {
	find(dir, files, callback);
    } else {
	callback(dir);
    }
}

function fileset(path, pattern) {
    var set = new Array()
    function callback(file) {
	set.push(file)
    }
    find(path, pattern, callback)
    return set
}

function find(dir, pattern, callback) {
    dir = pathToFile(dir);
    if (!callback) callback = println;
    var files = dir.listFiles();
    for (var f in files) {
	var file = files[f];
	if (file.isDirectory()) {
	    find(file, pattern, callback);
	} else {
	    if (pattern) {
		if (file.getName().matches(pattern)) {
		    callback(file);
		}
	    } else {
		callback(file);
	    }
	}
    }
}

function finddir(dir, callback) {
    dir = pathToFile(dir);
    if (!callback) callback = println;
    var files = dir.listFiles();
    for (var f in files) {
	var file = files[f];
	if (file.isDirectory()) {
	    finddir(file, callback);
	    callback(file);
	}
    }
}

function run(name) {
    var array = new Array(arguments.length - 1);
    for (var i = 0; i < array.length; i++) {
	array[i] = arguments[i+1];
    }
    Packages.linoleum.Tools.instance.run(name, curDir, array);
}

function javap(name) {
    Packages.com.sun.tools.javap.Main.run(["-c", "-classpath", curDir.getCanonicalPath(), name], new java.io.PrintWriter(java.lang.System.out));
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

function open(name, app) {
    uri = pathToFile(name).toURI();
    if (app == undefined) {
	frame.getApplicationManager().open(uri);
    } else {
	frame.getApplicationManager().open(app, uri);
    }
}

function exit(code) {
    frame.dispose();
}

function quit(code) {
    exit(code);
}

function install(pkg, conf, dir) {
    var instance = Packages.linoleum.pkg.PackageManager.instance;
    if (conf == undefined) {
	conf = "default";
    }
    if (dir == undefined) {
	instance.install(pkg, conf);
    } else {
	instance.install(pkg, conf, pathToFile(dir));
    }
}

function load(str) {
	var stream = inStream(str);
	var bstream = new BufferedInputStream(stream);
	var reader = new BufferedReader(new InputStreamReader(bstream));
	var oldFilename = engine.get(engine.FILENAME);
	engine.put(engine.FILENAME, str);	
	try {
		engine.eval(reader);
	} finally {
		engine.put(engine.FILENAME, oldFilename);
	        streamClose(stream);
	}
}

// adapted from https://weblogs.java.net/blog/forax/archive/2006/09/using_jrunscrip.html

function javac(srcDir, destDir) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    Packages.linoleum.Tools.instance.compile(fileset(srcDir, ".*\.java"), pathToFile(destDir), ["-source", "1.7", "-target", "1.7"]);
}

function classpath() {
    var str = "";
    var files = Packages.linoleum.Tools.instance.classpath();
    for(i in files) str += relativize(new File("."), files[i]).getPath() + (i < files.length - 1 ? java.io.File.pathSeparator : "");
    return str;
}

function javadoc(srcDir, destDir) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    var files = fileset(srcDir, ".*\.java");
    var dir = pathToFile(destDir);
    Packages.com.sun.tools.javadoc.Main.execute(["-classpath", classpath(), "-d", dir].concat(files));
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

function makepom(dest, source) {
    if (source == undefined) {
	source = "ivy.xml";
    }
    Packages.linoleum.pkg.PackageManager.instance.makepom(pathToFile(source), pathToFile(dest));
}

function publish(dir, resolver, source) {
    if (resolver == undefined) {
	resolver = "local";
    }
    if (source == undefined) {
	source = "ivy.xml";
    }
    Packages.linoleum.pkg.PackageManager.instance.publish(pathToFile(source), pathToFile(dir), resolver);
}

function clean(dir) {
    remove(dir, ".*\.class");
}

function remove(dir, pattern) {
    if (dir == undefined) {
	dir = ".";
    }
    if (pattern == undefined) {
	pattern = ".*";
    }
    find(dir, pattern, rm);
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
	println(fileToPath(file) + ":");
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
    if (!callback) callback = printfile;
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
    if (!callback) callback = printfile;
    var files = dir.listFiles();
    for (var f in files) {
	var file = files[f];
	if (file.isDirectory()) {
	    finddir(file, callback);
	    callback(file);
	}
    }
}

function printfile(file) {
    println(fileToPath(file));
}

function run(name) {
    var array = new Array(arguments.length - 1);
    for (var i = 0; i < array.length; i++) {
	array[i] = arguments[i+1];
    }
    Packages.linoleum.Tools.instance.run(name, curDir, array);
}

function javap(name) {
    Packages.com.sun.tools.javap.Main.run(["-c", "-classpath", curDir.getPath(), name], new java.io.PrintWriter(java.lang.System.out));
}

function fileToPath(file) {
    return java.util.regex.Pattern.compile("\\\\").matcher(relativize(curDir, file).getPath()).replaceAll("/");
}

function pathToFile(pathname) {
    var file = pathname
    if (!(file instanceof File)) {
	file = new File(pathname);
	if (!file.isAbsolute()) {
	    file = new File(curDir, pathname);
	}
    }
    return relativize(new File("."), file);
}

function relativize(baseDir, file) {
    var path = file.getCanonicalPath();
    var base = baseDir.getCanonicalPath();
    if (path.startsWith(base)) {
	if (path.equals(base)) {
	    path = ".";
	} else {
	    path = path.substring(base.length() + 1);
	}
    }
    return new File(path);
}

function ln(from, to) {
    var target = pathToFile(from);
    if (to == undefined) {
	to = target.getName();
    }
    Packages.linoleum.Tools.instance.mklink(pathToFile(to), target);
}

function wget(from, to) {
    var url = new java.net.URL(from);
    if (to == undefined) {
	var str = url.getFile();
	to = str.substring(str.lastIndexOf("/") + 1);
    }
    cp(url, pathToFile(to));
}

function open(name, app) {
    var uri = new java.net.URI(name);
    if (!uri.isAbsolute()) {
    	uri = pathToFile(name).toURI();
    }
    if (app == undefined) {
	frame.getApplicationManager().open(uri);
    } else {
	frame.getApplicationManager().open(app, uri);
    }
}

function exit(code) {
    frame.dispose();
}

function log(str) {
    Level = java.util.logging.Level;
    Logger = java.util.logging.Logger;
    var root = Logger.getLogger("");
    var handlers = root.getHandlers();
    if (handlers.length > 0) {
	var handler = handlers[0];
	if (handler.getLevel() > Level.CONFIG) {
	    handler.setLevel(Level.CONFIG);
	}
    }
    switch (str) {
    case "config":
	root.setLevel(Level.CONFIG);
	break;
    case "info":
	root.setLevel(Level.INFO);
	break;
    case "warning":
	root.setLevel(Level.WARNING);
	break;
    case "severe":
	root.setLevel(Level.SEVERE);
	break;
    }
}

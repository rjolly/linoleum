if (typeof(frame) == 'undefined') {
    apps = new Packages.linoleum.application.ApplicationManager();
    apps.manage(apps);
    apps.doOpen();
} else {
    apps = frame.getApplicationManager();
}

function getHome() {
    return new File(java.lang.System.getProperty("linoleum.home"));
}

function getLib() {
    return new File(getHome(), "lib");
}

function install(pkg, conf, dir) {
    if (conf == undefined) {
	conf = "default";
    }
    if (dir == undefined) {
	apps.get("Packages").install(pkg, conf);
    } else {
	apps.get("Packages").install(pkg, conf, pathToFile(dir));
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

function javac(srcDir, destDir, level) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    if (level == undefined) {
	level = "1.7";
    }
    apps.get("Tools").compile(fileset(srcDir, ".*\.java"), pathToFile(destDir), ["-source", level, "-target", level]);
}

function javadoc(srcDir, destDir) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    apps.get("Tools").javadoc(fileset(srcDir, ".*\.java"), pathToFile(destDir));
}

function copy(src, dest, pattern) {
    apps.get("Tools").copy(pathToFile(src), fileset(src, pattern), pathToFile(dest));
}

function jar(dest, dir, pattern, manifest) {
    if (dir == undefined) {
	dir = ".";
    }
    if (manifest == undefined) {
	apps.get("Tools").jar(pathToFile(dir), fileset(dir, pattern), pathToFile(dest));
    } else {
	apps.get("Tools").jar(pathToFile(dir), fileset(dir, pattern), pathToFile(dest), pathToFile(manifest));
    }
}

function makepom(dest, source) {
    if (source == undefined) {
	source = "ivy.xml";
    }
    apps.get("Packages").makepom(pathToFile(source), pathToFile(dest));
}

function publish(dir, resolver, source) {
    if (resolver == undefined) {
	resolver = "local";
    }
    if (source == undefined) {
	source = "ivy.xml";
    }
    apps.get("Packages").publish(pathToFile(source), pathToFile(dir), resolver);
}

function clean(dir) {
    remove(dir, ".*\.(class|tasty)");
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

function kill(name) {
    apps.get("Tools").getThread(name).stop();
}

function run(name, dir) {
    var offset = 2;
    if (dir == undefined) {
	offset = 1;
	dir = ".";
    }
    var array = new Array(arguments.length - offset);
    for (var i = 0; i < array.length; i++) {
	array[i] = arguments[i + offset];
    }
    apps.get("Tools").run(name, pathToFile(dir), array);
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
	    file = new File(curDir, pathname).getCanonicalFile();
	}
    }
    return relativize(new File(".").getCanonicalFile(), file);
}

function relativize(baseDir, file) {
    var path = file.getPath();
    var base = baseDir.getPath();
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
    apps.get("Tools").mklink(pathToFile(to), target);
}

function wget(from, to) {
    var url = new java.net.URL(from);
    if (to == undefined) {
	to = new File(url.getFile()).getName();
    }
    cp(url, pathToFile(to));
}

function open(name, app) {
    var uri = new java.net.URI(name);
    if (!uri.isAbsolute()) {
	uri = pathToFile(name).toURI();
    }
    if (app == undefined) {
	apps.open(uri);
    } else {
	apps.get(app).open(uri, frame.getDesktopPane());
    }
}

function exit(code) {
    if (typeof(frame) == 'undefined') {
	if (code) {
	    java.lang.System.exit(code + 0);
	} else {
	    java.lang.System.exit(0);
	}
    } else {
	frame.doDefaultCloseAction();
    }
}

function quit(code) {
    exit(code);
}

function log(str) {
    Level = java.util.logging.Level;
    var root = java.util.logging.Logger.getLogger("");
    switch (str) {
    case "finest":
	root.setLevel(Level.FINEST);
	break;
    case "finer":
	root.setLevel(Level.FINER);
	break;
    case "fine":
	root.setLevel(Level.FINE);
	break;
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

function gc() {
    java.lang.System.gc();
}

function version() {
    return java.lang.System.getProperty("java.version");
}

function exec(command) {
    return java.lang.Runtime.getRuntime().exec(command);
}

// requires commons-io#commons-io;1.3.1

function hexdump(path) {
    System = java.lang.System;
    Files = java.nio.file.Files;
    HexDump = Packages.org.apache.commons.io.HexDump;
    HexDump.dump(Files.readAllBytes(pathToFile(path).toPath()), 0, System.out, 0);
}

// requires com.googlecode.java-diff-utils#diffutils;1.3.0

function diff(file1, file2) {
    DiffUtils = Packages.difflib.DiffUtils;
    IOUtils = Packages.org.apache.commons.io.IOUtils;
    FileUtils = Packages.org.apache.commons.io.FileUtils;
    file1 = pathToFile(file1);
    file2 = pathToFile(file2);
    is1 = FileUtils.openInputStream(file1);
    is2 = FileUtils.openInputStream(file2);
    var a = IOUtils.readLines(is1);
    var b = IOUtils.readLines(is2);
    is1.close();
    is2.close();
    var p = DiffUtils.diff(a, b);
    var d = DiffUtils.generateUnifiedDiff(file1.getName(), file2.getName(), a, p, 3);
    for (var i = 0; i < d.size(); i++) println(d.get(i));
}

// requires org.eclipse.jgit#org.eclipse.jgit;3.4.0.201406110918-r

function git() {
    return Packages.org.eclipse.jgit.api.Git.open(pathToFile(".git"));
}

function clone(str, dir) {
    var uri = new java.net.URI(str);
    if (dir == undefined) {
	var name = new File(uri.getPath()).getName();
	var n = name.lastIndexOf(".git");
	dir = n < 0 ? name : name.substring(0, n);
    }
    return Packages.org.eclipse.jgit.api.Git.cloneRepository().setDirectory(pathToFile(dir)).setURI(uri.toString()).call();
}

// requires org.ghost4j#ghost4j;1.0.1 and Ghostscript
// https://downloads.sourceforge.net/ghostscript/gs905w64.exe

function ps2pdf(file, output) {
    PSDocument = Packages.org.ghost4j.document.PSDocument;
    PDFConverter = Packages.org.ghost4j.converter.PDFConverter;
    IOUtils = Packages.org.apache.commons.io.IOUtils;
    FileOutputStream = java.io.FileOutputStream;
    file = pathToFile(file);
    if (output == undefined) {
	var name = file.getName();
	var n = name.lastIndexOf(".ps");
	if (n > 0) {
	    name = name.substring(0, n);
	}
	output = name + ".pdf";
    }
    document = new PSDocument();
    document.load(file);
    fos = new FileOutputStream(pathToFile(output));
    converter = new PDFConverter();
    converter.setPDFSettings(PDFConverter.OPTION_PDFSETTINGS_PREPRESS);
    converter.convert(document, fos);
    IOUtils.closeQuietly(fos);
}

function classpath() {
    return java.lang.System.getProperty("java.class.path");
}

// requires ch.epfl.lamp#dotty-compiler_0.27;0.27.0-RC1

function dotc(srcDir, destDir, options) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    if (options == undefined) {
	options = [];
    }
    files = fileset(srcDir, ".*\.(scala|java)");
    dir = pathToFile(destDir);
    Packages.dotty.tools.dotc.Main.process(["-color:never", "-classpath", classpath(), "-d", dir].concat(options).concat(files));
}

// requires org.scala-lang#scala-compiler;2.11.0-M6

function scalac(srcDir, destDir, options) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    if (options == undefined) {
	options = [];
    }
    files = fileset(srcDir, ".*\.(scala|java)");
    dir = pathToFile(destDir);
    Packages.scala.tools.nsc.Main.process(["-classpath", classpath(), "-d", dir].concat(options).concat(files));
}

function scaladoc(srcDir, destDir, options) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    if (options == undefined) {
	options = [];
    }
    var files = fileset(srcDir, ".*\.(scala|java)");
    var dir = pathToFile(destDir);
    Packages.scala.tools.nsc.ScalaDoc$.MODULE$.process(["-classpath", classpath(), "-d", dir].concat(options).concat(files));
}

// requires com.github.rjolly#jarlister_2.11;1.1

function jarlister(path, out) {
    var opts = [];
    if (out != undefined) {
	opts = opts.concat(["-o", pathToFile(out)]);
    }
    Packages.scala.tools.nsc.JarLister$.MODULE$.process(opts.concat([pathToFile(path)]));
}

// requires net.sourceforge.jscl-meditor#txt2xhtml;4.1

function txt2xhtml(srcDir, destDir, stylesheet, feed, icon) {
    if (srcDir == undefined) {
	srcDir = ".";
    }
    if (destDir == undefined) {
	destDir = srcDir;
    }
    if (stylesheet == undefined) {
	stylesheet = "/mathmlc2p.xsl";
    }
    if (feed == undefined) {
	feed = null;
    }
    if (icon == undefined) {
	icon = null;
    }
    destDir = pathToFile(destDir);
    srcDir = pathToFile(srcDir);
    function callback(file) {
	FileReader = java.io.FileReader;
	FileWriter = java.io.FileWriter;
	var converter = new Packages.jscl.converter.Converter();
	var str = relativize(srcDir, file).getPath();
	str = str.substring(0, str.lastIndexOf("."));
	var out = new File(destDir, str + ".xhtml");
	var parent = out.getParentFile();
	if (parent != null) {
		parent.mkdirs();
	}
	var reader = new BufferedReader(new FileReader(file));
	var writer = new FileWriter(out);
	try {
	    converter.apply(reader, stylesheet, str, feed, icon, null, true, writer);
	} finally {
	    writer.close();
	    reader.close();
	}
    }
    find(srcDir, ".*\.txt", callback)
}

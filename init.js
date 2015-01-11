function install(pkg) {
    PackageManager = Packages.linoleum.PackageManager;
    PackageManager.instance.install([pkg]);
}

function javac(fileset, destDir) {
    ToolProvider = javax.tools.ToolProvider;
    StandardLocation = javax.tools.StandardLocation;
    Arrays = java.util.Arrays;
    File = java.io.File;

    compiler = ToolProvider.getSystemJavaCompiler()
    fileManager = compiler.getStandardFileManager(null, null, null)
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList([pathToFile(destDir)].valueOf()))

    compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(fileset.valueOf()));
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

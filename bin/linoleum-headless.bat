@echo off
set LINOLEUM_HOME=%~dp0..
jrunscript -J-Dfile.encoding=UTF-8 -J-Djava.system.class.loader=linoleum.application.ClassLoader -J--add-opens -Jjdk.jdeps/com.sun.tools.javap=ALL-UNNAMED -J--add-opens -Jjdk.jconsole/sun.tools.jconsole=ALL-UNNAMED -J-Djava.class.path=%LINOLEUM_HOME%\linoleum.jar;%LINOLEUM_HOME%\lib\nashorn-core-15.4.jar;%LINOLEUM_HOME%\lib\asm-7.3.1.jar";%LINOLEUM_HOME%\lib\asm-analysis-7.3.1.jar";%LINOLEUM_HOME%\lib\asm-commons-7.3.1.jar;%LINOLEUM_HOME%\lib\asm-tree-7.3.1.jar;%LINOLEUM_HOME%\lib\asm-util-7.3.1.jar -f %LINOLEUM_HOME%\init.js %*

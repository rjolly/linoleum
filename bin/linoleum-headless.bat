@echo off
set LINOLEUM_HOME=%~dp0..
jrunscript -J-Dfile.encoding=UTF-8 -J-Djava.system.class.loader=linoleum.application.ClassLoader -J--add-opens -Jjdk.jdeps/com.sun.tools.javap=ALL-UNNAMED -J--add-opens -Jjdk.jconsole/sun.tools.jconsole=ALL-UNNAMED -J-Djava.class.path=%LINOLEUM_HOME%\linoleum.jar -f %LINOLEUM_HOME%\init.js %*

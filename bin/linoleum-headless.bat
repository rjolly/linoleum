@echo off
set LINOLEUM_HOME=%~dp0..
jrunscript -J-Dfile.encoding=UTF-8 -J-Djava.system.class.loader=linoleum.application.ClassLoader -J-Djava.class.path=%JAVA_HOME%\lib\tools.jar;%LINOLEUM_HOME%\linoleum.jar -f %LINOLEUM_HOME%\init.js %*

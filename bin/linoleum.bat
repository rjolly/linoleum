@echo off
set LINOLEUM_HOME=%~dp0..
java -Dfile.encoding=UTF-8 -Dlinoleum.home=%LINOLEUM_HOME% -Djava.system.class.loader=linoleum.ClassLoader -jar %LINOLEUM_HOME%\linoleum.jar

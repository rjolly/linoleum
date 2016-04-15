@echo off
set LINOLEUM_HOME=%~dp0..
java -Dfile.encoding=UTF-8 -Djava.system.class.loader=linoleum.ClassLoader -jar %LINOLEUM_HOME%\linoleum.jar

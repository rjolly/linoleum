@echo off
cd "%~dp0.."
java -Dfile.encoding=UTF-8 -Djava.system.class.loader=linoleum.ClassLoader -jar linoleum.jar

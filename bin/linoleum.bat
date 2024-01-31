@echo off
set LINOLEUM_HOME=%~dp0..
java -Dfile.encoding=UTF-8 -Djava.system.class.loader=linoleum.application.ClassLoader -Dscala.usejavacp=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false -jar %LINOLEUM_HOME%\linoleum.jar

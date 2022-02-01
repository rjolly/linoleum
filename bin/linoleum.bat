@echo off
set LINOLEUM_HOME=%~dp0..
set OPTS=
java -classpath %LINOLEUM_HOME%\linoleum.jar linoleum.Version9
if %ERRORLEVEL% EQU 0 (
    set OPTS=--add-opens jdk.jconsole/sun.tools.jconsole=ALL-UNNAMED --add-opens java.scripting/com.sun.tools.script.shell=ALL-UNNAMED --add-opens java.desktop/java.awt=ALL-UNNAMED --add-opens java.desktop/javax.swing.text=ALL-UNNAMED --add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED
)
java %OPTS% -Dfile.encoding=UTF-8 -Djava.system.class.loader=linoleum.application.ClassLoader -Dscala.usejavacp=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false -jar %LINOLEUM_HOME%\linoleum.jar

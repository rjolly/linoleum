@echo off
set LINOLEUM_HOME=%~dp0..
java --add-opens jdk.jconsole/sun.tools.jconsole=ALL-UNNAMED --add-opens java.scripting/com.sun.tools.script.shell=ALL-UNNAMED --add-opens java.desktop/java.awt=ALL-UNNAMED --add-opens java.desktop/javax.swing.text=ALL-UNNAMED --add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED -Dfile.encoding=UTF-8 -Djava.system.class.loader=linoleum.application.ClassLoader -Dscala.usejavacp=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false -jar %LINOLEUM_HOME%\linoleum.jar

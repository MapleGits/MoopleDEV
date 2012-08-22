@echo off
@title MoopleDEV's INI creator
set CLASSPATH=.;dist\*
java -Xmx100m net.server.CreateINI
pause
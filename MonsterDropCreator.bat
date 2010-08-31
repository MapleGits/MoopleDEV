@echo off
set CLASSPATH=.;dist\MoopleDEV.jar;dist\exttools.jar;mina-core.jar;slf4j-api.jar;slf4j-jdk14.jar;mysql-connector-java-bin.jar;
java -Dwzpath=wz\ tools.MonsterDropCreator false
pause
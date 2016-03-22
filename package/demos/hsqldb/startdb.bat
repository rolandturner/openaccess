@echo off
REM Setup environment vars and run HSQLDB

if ."%JAVA_HOME%".==.. goto nojavahome
goto havejavahome
:nojavahome
echo Please set JAVA_HOME
exit /B 1

:havejavahome

cd %~dp0
echo Using JAVA_HOME %JAVA_HOME%

"%JAVA_HOME%\bin\java" -cp ..\..\lib\hsqldb.jar org.hsqldb.Server

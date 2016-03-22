@echo off
REM Setup environment vars

if "%OS%"=="Windows_NT" @setlocal

if ."%JAVA_HOME%".==.. goto nojavahome
goto havejavahome

:nojavahome
echo Please set JAVA_HOME before running setup-env.bat
exit /B 1

:havejavahome

set OPENACCESS_HOME=%~dp0..\

#cd %OPENACCESS_HOME%

set CP=%OPENACCESS_HOME%\license\
for %%i in ("%OPENACCESS_HOME%\lib\*.jar") do call %OPENACCESS_HOME%\bin\lcp.bat "%%i"
for %%i in ("%OPENACCESS_HOME%\tools\*.jar") do call %OPENACCESS_HOME%\bin\lcp.bat "%%i"

echo Using JAVA_HOME %JAVA_HOME%
echo Using OPENACCESS_HOME %OPENACCESS_HOME%
echo CLASSPATH %CP%

#"%JAVA_HOME%\bin\java" -cp %CP% -DOPENACCESS_HOME="%OPENACCESS_HOME%\" com.versant.core.jdo.tools.workbench.Main

if "%OS%"=="Windows_NT" @endlocal


set CLASSPATH=%CP%;%CLASSPATH%
set PATH=%OPENACCESS_HOME%/bin;%JDKHOME%/bin;%PATH%

#cd ..
call command

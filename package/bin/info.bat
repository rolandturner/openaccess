@echo off
REM Setup environment vars and run Versant OpenAccess Workbench

@setlocal

if ."%JAVA_HOME%".==.. goto nojavahome
goto havejavahome

:nojavahome
echo Please set JAVA_HOME before running workbench.bat
exit /B 1

:havejavahome
set OPENACCESS_HOME=%~dp0..\
cd %OPENACCESS_HOME%
set CP=license\
for %%i in ("lib\*.jar") do call bin\lcp.bat "%%i"
for %%i in ("tools\*.jar") do call bin\lcp.bat "%%i"

echo Using JAVA_HOME %JAVA_HOME%
echo Using OPENACCESS_HOME %OPENACCESS_HOME%
echo CLASSPATH %CP%

"%JAVA_HOME%\bin\java" -cp %CP% -DOPENACCESS_HOME="%OPENACCESS_HOME%\" com.versant.core.jdo.license.Info

pause
@endlocal
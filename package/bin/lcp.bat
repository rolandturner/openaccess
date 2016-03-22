REM Helper batch file to build the classpath for workbench.bat (adapted from Ant)

set LCP=%1
:argcheck
if %2a==a goto done
shift
set LCP=%LCP%;%1
goto argcheck

:done
set CP=%CP%;%LCP%

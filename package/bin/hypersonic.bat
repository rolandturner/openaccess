@setlocal
@REM Start a Hypersonic server running databases for all the demos
@setlocal
cd %~dp0../
call demos/hsqldb/startdb.bat
cd %~dp0../
@endlocal
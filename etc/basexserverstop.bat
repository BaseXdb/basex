@setlocal
@echo off

REM Path to this script
set PWD=%~dp0

REM Paths to distributed files or source directories
set BASEX=%PWD%/../target/classes

REM Run code
java -cp "%BASEX%" org.basex.BaseXServer stop

@endlocal

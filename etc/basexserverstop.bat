@setlocal
@echo off

REM Path to this script
set PWD=%~dp0

REM Paths to distributed files or source directories
REM set BXPATH=basex.jar
set BXPATH=%PWD%/../target/classes

REM Run BaseX server
java -cp "%BXPATH%" %VM% org.basex.BaseXServer stop

@endlocal

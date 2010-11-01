@setlocal
@echo off

REM Path to this script
set PWD=%~dp0

REM Paths to distributed files or source directories
REM set BXPATH=basex.jar
set BXPATH=%PWD%/../target/classes

REM Options for virtual machine
set VM=-Xmx1g

REM Run BaseX GUI
java -cp "%BXPATH%" %VM% org.basex.BaseXGUI %*

@endlocal

@echo off
setlocal

REM Path to this script
set PWD=%~dp0

REM Paths to distributed files or source directories
set BASEX=%PWD%/../target/classes

REM Options for virtual machine
set VM=-Xmx512m

REM Run code
java -cp "%BASEX%" %VM% org.basex.BaseXGUI %*

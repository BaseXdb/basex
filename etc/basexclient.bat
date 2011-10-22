@echo off
setlocal

REM Path to this script
set PWD=%~dp0

REM Core and library classes
set CP=%PWD%/../target/classes

REM Run code
java -cp "%CP%" org.basex.BaseXClient %*

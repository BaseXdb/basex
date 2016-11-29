@echo off
setLocal EnableDelayedExpansion

REM Path to core and library classes
set MAIN=%~dp0/..
set CP=%MAIN%/target/classes;%MAIN%/lib/*

REM Run code
java -cp "%CP%" %BASEX_JVM% org.basex.BaseXServer %* stop

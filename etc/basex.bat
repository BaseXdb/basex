@echo off
setlocal

REM Path to this script
set PWD=%~dp0

REM Paths to distributed files or source directories
set BASEX=%PWD%/../target/classes

REM Options for virtual machine
set VM=-Xmx512m

REM Classpath
set LIB=%PWD%/../lib
set CP=%BASEX%;%LIB%/lucene-analyzers-3.0.2.jar;%LIB%/tagsoup-1.2.jar;%LIB%/snowball.jar

REM Run code
java -cp "%CP%" %VM% org.basex.BaseX %*

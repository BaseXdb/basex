@echo off
setlocal

REM Path to this script
set PWD=%~dp0

REM Core and library classes
set CP=%PWD%/../target/classes
set LIB=%PWD%/../lib
set CP=%CP%;%LIB%/igo-0.4.3.jar;%LIB%/lucene-stemmers-3.4.0.jar;%LIB%/xml-resolver-1.2.jar;%LIB%/tagsoup-1.2.jar

REM Options for virtual machine
set VM=-Xmx512m

REM Run code
java -cp "%CP%" %VM% org.basex.BaseXGUI %*

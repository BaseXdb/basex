@echo off
setlocal

REM Path to this script
set PWD=%~dp0

REM Paths to distributed files or source directories
set BASEX=%PWD%/../../basex/target/classes
set BASEXAPI=%PWD%/../target/classes

REM Classpath
set LIB=%PWD%/../lib
set CP=%BASEX%;%BASEXAPI%;%LIB%/commons-fileupload-1.2.2.jar;%LIB%/jetty-6.1.26.jar;%LIB%/jetty-util-6.1.26.jar;%LIB%/lucene-analyzers-3.0.2.jar;%LIB%/milton-api-1.6.4.jar;%LIB%/resolver.jar;%LIB%/servlet-api-2.5-20081211.jar;%LIB%/snowball.jar;%LIB%/tagsoup-1.2.jar

REM Options for virtual machine
set VM=-Xmx512m

REM Run code
java -cp "%CP%;." %VM% org.basex.api.BaseXHTTP %*

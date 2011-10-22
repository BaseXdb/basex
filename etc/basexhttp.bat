@echo off
setlocal

REM Path to this script
set PWD=%~dp0

REM Core and library classes
set CP=%PWD%/../target/classes
set LIB=%PWD%/../lib
set CP=%CP%;%LIB%/commons-codec-1.4.jar;%LIB%/commons-fileupload-1.2.2.jar;%LIB%/commons-io-1.4.jar;%LIB%/jdom-1.1.jar;%LIB%/jetty-6.1.26.jar;%LIB%/jetty-util-6.1.26.jar;%LIB%/milton-api-1.6.5.jar;%LIB%/mime-util-2.1.3.jar;%LIB%/servlet-api-2.5-20081211.jar;%LIB%/slf4j-api-1.5.8.jar;%LIB%/slf4j-nop-1.5.8.jar;%LIB%/xmldb-api-1.0.jar;%LIB%/xqj-api-1.0.jar
set CP=%CP%;%PWD%/../../basex/target/classes
set LIB=%PWD%/../../basex/lib
set CP=%CP%;%LIB%/igo-0.4.3.jar;%LIB%/lucene-stemmers-3.4.0.jar;%LIB%/tagsoup-1.2.jar;%LIB%/xml-resolver-1.2.jar

REM Options for virtual machine
set VM=-Xmx512m

REM Run code
java -cp "%CP%;." %VM% org.basex.api.BaseXHTTP %*

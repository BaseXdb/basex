@echo off
setlocal

REM Path to this script
set PWD=%~dp0

REM Core and library classes
set CP=%PWD%/../target/classes
set LIB=%PWD%/../lib
set CP=%CP%;%LIB%/commons-codec-1.4.jar;%LIB%/commons-fileupload-1.2.2.jar;%LIB%/commons-io-1.4.jar;%LIB%/javax.servlet-3.0.0.v201112011016.jar;%LIB%/jdom-1.1.jar;%LIB%/jetty-continuation-8.1.4.v20120524.jar;%LIB%/jetty-http-8.1.4.v20120524.jar;%LIB%/jetty-io-8.1.4.v20120524.jar;%LIB%/jetty-security-8.1.4.v20120524.jar;%LIB%/jetty-server-8.1.4.v20120524.jar;%LIB%/jetty-servlet-8.1.4.v20120524.jar;%LIB%/jetty-util-8.1.4.v20120524.jar;%LIB%/milton-api-1.7.2.jar;%LIB%/mime-util-2.1.3.jar;%LIB%/xmldb-api-1.0.jar;%LIB%/xqj-api-1.0.jar;%LIB%/xqj2-0.1.0.jar;%LIB%/jetty-webapp-8.1.4.v20120524.jar;%LIB%/jetty-servlets-8.1.4.v20120524.jar;%LIB%/jetty-xml-8.1.4.v20120524.jar
set LIB=%PWD%/../../basex/lib
set CP=%CP%;%LIB%/igo-0.4.3.jar;%LIB%/lucene-stemmers-3.4.0.jar;%LIB%/tagsoup-1.2.jar;%LIB%/xml-resolver-1.2.jar;%LIB%/jline-1.0.jar

REM Options for virtual machine
set VM=

REM Run code
java -cp "%CP%;." %VM% org.basex.BaseXHTTP %* stop

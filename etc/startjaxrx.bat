@setlocal
@echo off

REM Path to this script
set PWD=%~dp0

REM Needs to be checked: Path to BaseX binary
set BXPATH=%PWD%/BaseX61.jar

REM Classpath
set LIB=%PWD%/lib
set CP=BaseX61-jaxrx.jar;%BXPATH%;%LIB%/jetty-6.1.21.jar;%LIB%/jetty-util-6.1.21.jar;%LIB%/jsr311-api-1.0.jar;%LIB%/servlet-api-2.5-20081211.jar;%LIB%/jersey-server-1.0.1.jar;%LIB%/jersey-core-1.0.1.jar;%LIB%/asm-3.1.jar

REM Assign more memory
set VM=-Xmx1g

REM Run server
java -cp "%CP%;." %VM% StartJaxRx

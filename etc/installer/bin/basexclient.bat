@setlocal
@echo off

REM Path to this script
set PWD=%~dp0

REM Paths to distributed files or source directories
set BXPATH=%PWD%/../BaseX.jar
REM set BXPATH=%PWD%/../target/classes

REM Options for virtual machine
set VM=

REM Run BaseX client
java -cp "%BXPATH%" %VM% org.basex.BaseXClient %*

@endlocal

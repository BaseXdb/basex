@setlocal
@echo off

REM Path to .class files (target\classes) or BaseX.jar
set BXPATH=%~dp0\..\target\classes

REM Options for virtual machine
set VM=-Xmx1g

REM Run BaseX
java -cp "%BXPATH%" %VM% org.basex.BaseX %*

@endlocal

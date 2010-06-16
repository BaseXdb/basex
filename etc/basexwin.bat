@setlocal
@echo off

REM Path to .class files (target\classes) or BaseX.jar
set BXPATH=%~dp0\..\target\classes

REM Options for virtual machine
set VM=-Xmx1g

REM Run BaseX
javaw -cp "%BXPATH%" %VM% org.basex.BaseXWin %*

@endlocal

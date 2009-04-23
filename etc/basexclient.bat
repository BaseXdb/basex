@echo off
setlocal

REM Path to class directory (bin) or BaseX.jar
set BXPATH=%~dp0\..\bin

REM Java options (virtual memory in MB)
set JMEM=-Xmx1024m

REM Run BaseX
java %JMEM% -cp "%BXPATH%" org.basex.BaseXClient %*

endlocal

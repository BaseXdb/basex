@setlocal
@echo off

REM Path to class directory (bin) or BaseX.jar
set BXPATH=%~dp0\..\bin

REM Java options (virtual memory in MB)
set VM=-Xmx1g

REM Run BaseX
java -cp "%BXPATH%" %VM% org.basex.BaseX %*

@endlocal

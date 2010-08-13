@echo off
IF %1!==! goto end
IF "%1" == "start" (
    java -cp %BASE%\basex-6.2.4.jar org.basex.BaseXServer
  ) ELSE (
    java -cp %BASE%\basex-6.2.4.jar org.basex.BaseXServer %1
  )
)
:end
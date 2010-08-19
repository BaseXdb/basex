!define JAR "Basex-6.2.4.jar"
!define PRODUCT_NAME "BaseX"
!define PRODUCT_VERSION "6.2.4"
!define PRODUCT_PUBLISHER "DBIS Group, University of Konstanz"
!define PRODUCT_WEB_SITE "http://www.basex.org"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\${JAR}"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

RequestExecutionLevel admin

; MUI 1.67 compatible ------
!include "MUI.nsh"
!include "FileFunc.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "..\images\Basex.ico" ;"${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
Function .onInit
!insertmacro MUI_INSTALLOPTIONS_EXTRACT_AS "Options.ini" "Options"
FunctionEnd

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!define MUI_LICENSEPAGE_RADIOBUTTONS
!insertmacro MUI_PAGE_LICENSE "..\..\license.txt"
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Custom page
Page custom OptionsPage OptionsLeave
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION run_basex
!define MUI_FINISHPAGE_SHOWREADME ""
!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
!define MUI_FINISHPAGE_SHOWREADME_TEXT "Install BaseXServer as service"
!define MUI_FINISHPAGE_SHOWREADME_FUNCTION install_service
!insertmacro MUI_PAGE_FINISH

Function install_service
         nsExec::Exec '$INSTDIR\service\bin\InstallService.bat'
FunctionEnd

Function run_basex
        nsExec::Exec '$INSTDIR\${PRODUCT_NAME}${PRODUCT_VERSION}.exe'
FunctionEnd

# CUSTOM PAGE.
# =========================================================================
#
Function OptionsPage
# Display the page.
!insertmacro MUI_INSTALLOPTIONS_DISPLAY "Options"
FunctionEnd
Function OptionsLeave
# Get the user entered values.
!insertmacro MUI_INSTALLOPTIONS_READ $R0 "Options" "Field 2" "State"
!insertmacro MUI_INSTALLOPTIONS_READ $R1 "Options" "Field 3" "State"
!insertmacro MUI_INSTALLOPTIONS_READ $R2 "Options" "Field 8" "State"
!insertmacro MUI_INSTALLOPTIONS_READ $R3 "Options" "Field 9" "State"
        ${If} $R1 == $R0
              ${If} $R0 != "admin"
                    nsExec::Exec 'java -cp ${JAR} org.basex.BaseX -c alter user admin $R0'
              ${EndIf}
        ${Else}
          MessageBox MB_OK "Passwords do not match."
          Abort
        ${EndIf}
        CreateDirectory "$INSTDIR\BaseXData"
        ; .xq file Association
        ${If} $R2 == 1
          WriteRegStr HKCR ".xq" "" "File.xq"
          WriteRegStr HKCR "File.xq" "" "XQuery File"
          WriteRegStr HKCR "File.xq\shell" "" "Open"
          WriteRegStr HKCR "File.xq\shell\Open\command" "" "$INSTDIR\${PRODUCT_NAME}${PRODUCT_VERSION}.exe %1"
          WriteRegStr HKCR "File.xq\DefaultIcon" "" "$INSTDIR\xml.ico"
        ${EndIf}
        ; .xml file Association
        ${If} $R3 == 1
          WriteRegStr HKCR ".xml" "" "File.xml"
          WriteRegStr HKCR "File.xml" "" "XML File"
          WriteRegStr HKCR "File.xml\shell" "" "Open"
          WriteRegStr HKCR "File.xml\shell\Open\command" "" "$INSTDIR\${PRODUCT_NAME}${PRODUCT_VERSION}.exe %1"
          WriteRegStr HKCR "File.xml\DefaultIcon" "" "$INSTDIR\xml.ico"
        ${EndIf}
        ${RefreshShellIcons}
FunctionEnd

; Language files
!insertmacro MUI_LANGUAGE "English"
; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "Setup.exe"
InstallDir "$PROGRAMFILES\BaseX"
InstallDirRegKey HKLM "${PRODUCT_DIR_REGKEY}" ""
ShowInstDetails show
ShowUnInstDetails show

Section "Hauptgruppe" SEC01
  SetOutPath "$INSTDIR"
  SetOverwrite ifnewer
  File "..\..\target\${JAR}"
  File "..\..\license.txt"
  File ".basex"
  File "..\images\xml.ico"
  File "..\images\BaseX.ico"
  File "${PRODUCT_NAME}${PRODUCT_VERSION}.exe"
  nsExec::Exec 'java -cp ${JAR} org.basex.BaseX -Wc set dbpath $INSTDIR\BaseXData'
  SetOutPath "$INSTDIR\service\bin"
  File "service\bin\BaseX.bat"
  File "service\bin\StartService.bat"
  File "service\bin\InstallService.bat"
  File "service\bin\StopService.bat"
  File "service\bin\UninstallService.bat"
  File "service\bin\wrapper.exe"
  SetOutPath "$INSTDIR\service\lib"
  File "service\lib\wrapper.dll"
  File "service\lib\wrapper.jar"
  SetOutPath "$INSTDIR\service\conf"
  File "service\conf\wrapper.conf"
  CreateDirectory "$INSTDIR\service\logs"
SectionEnd

Section -AdditionalIcons
  !insertmacro MUI_INSTALLOPTIONS_READ $R4 "Options" "Field 7" "State"
  !insertmacro MUI_INSTALLOPTIONS_READ $R3 "Options" "Field 6" "State"
  ${If} $R3 == 1
    CreateShortCut "$DESKTOP\BaseX.lnk" "$INSTDIR\${JAR}" "" "$INSTDIR\BaseX.ico" 0
  ${EndIf}
  ${If} $R4 == 1
    CreateDirectory "$SMPROGRAMS\BaseX"
    CreateShortCut "$SMPROGRAMS\BaseX\BaseX.lnk" "$INSTDIR\${JAR}" "" "$INSTDIR\BaseX.ico" 0
    WriteINIStr "$SMPROGRAMS\BaseX\Website.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
    CreateShortCut "$SMPROGRAMS\BaseX\StartService.lnk" "$INSTDIR\service\startService.bat"
    CreateShortCut "$SMPROGRAMS\BaseX\StopService.lnk" "$INSTDIR\service\stopService.bat"
    CreateShortCut "$SMPROGRAMS\BaseX\Uninstall.lnk" "$INSTDIR\uninst.exe"
  ${EndIf}
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd


Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) has been uninstalled from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "All components of $(^Name) will be uninstalled??" IDYES +2
  Abort
  nsExec::Exec 'net stop BaseXServer'
  nsExec::Exec 'sc delete BaseXServer'
FunctionEnd

Section Uninstall
  Delete "$DESKTOP\BaseX.lnk"
  RMDir /r "$SMPROGRAMS\BaseX"
  RMDIR /r "$INSTDIR\service"
  RMDir /r "$INSTDIR"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  DeleteRegKey HKLM "${PRODUCT_DIR_REGKEY}"
  DeleteRegKey HKCR ".xq"
  DeleteRegKey HKCR "File.xq\shell\Open\command"
  DeleteRegKey HKCR "File.xq\DefaultIcon"
  DeleteRegKey HKCR "File.xq"
  DeleteRegKey HKCR ".xml"
  DeleteRegKey HKCR "File.xml\shell\Open\command"
  DeleteRegKey HKCR "File.xml\DefaultIcon"
  DeleteRegKey HKCR "File.xml"
  ${RefreshShellIcons}
  SetAutoClose true
SectionEnd
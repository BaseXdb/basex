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
!insertmacro MUI_PAGE_LICENSE "license.txt"
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
         nsExec::Exec '$INSTDIR\service\installService.bat'
FunctionEnd

Function run_basex
        Exec 'javaw -jar ${JAR}'
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
        ${If} $R1 == $R0
              ${If} $R0 != "admin"
                    nsExec::Exec 'java -cp ${JAR} org.basex.BaseX -c alter user admin $R0'
              ${EndIf}
        ${Else}
          MessageBox MB_OK "Passwords do not match."
          Abort
        ${EndIf}
        CreateDirectory "$INSTDIR\BaseXData"
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
  File "..\images\BaseX.ico"
  File "License.txt"
  File ".basex"
  nsExec::Exec 'java -cp ${JAR} org.basex.BaseX -Wc set dbpath $INSTDIR\BaseXData'
  SetOutPath "$INSTDIR\service\lib\core\commons"
  File "service\lib\core\commons\commons-cli-2.jar"
  File "service\lib\core\commons\commons-codec-1.3.jar"
  File "service\lib\core\commons\commons-collections-3.2.jar"
  File "service\lib\core\commons\commons-configuration-1.7-SNAPSHOT.jar"
  File "service\lib\core\commons\commons-httpclient-3.0.1.jar"
  File "service\lib\core\commons\commons-io-1.3.1.jar"
  File "service\lib\core\commons\commons-jexl-1.1.jar"
  File "service\lib\core\commons\commons-lang-2.4.jar"
  File "service\lib\core\commons\commons-logging-1.1.jar"
  File "service\lib\core\commons\commons-net-1.4.1.jar"
  File "service\lib\core\commons\commons-vfs-2.0-SNAPSHOT.jar"
  SetOutPath "$INSTDIR\service\lib\core\groovy"
  File "service\lib\core\groovy\groovy-all-1.7.2.jar"
  SetOutPath "$INSTDIR\service\lib\core\jna"
  File "service\lib\core\jna\jna-3.2.5.jar"
  SetOutPath "$INSTDIR\service\lib\core\netty"
  File "service\lib\core\netty\netty-3.2.1.Final.jar"
  SetOutPath "$INSTDIR\service\lib\core"
  File "service\lib\core\ReadMe.txt"
  SetOutPath "$INSTDIR\service\lib\core\regex"
  File "service\lib\core\regex\jrexx-1.1.1.jar"
  SetOutPath "$INSTDIR\service"
  File "service\installService.bat"
  File "service\setenv.bat"
  File "service\startService.bat"
  File "service\stopService.bat"
  File "service\uninstallService.bat"
  File "service\wrapper.bat"
  File "service\wrapper.conf"
  File "service\wrapper.jar"
  File "service\wrapperApp.jar"
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
  SetAutoClose true
SectionEnd
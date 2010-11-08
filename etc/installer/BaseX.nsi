!define JAR "BaseX.jar"
!define PRODUCT_NAME "BaseX"
!define PRODUCT_PUBLISHER "DBIS Group, University of Konstanz"
!define PRODUCT_WEB_SITE "http://www.basex.org"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\${JAR}"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"
!define ALPHA "abcdefghijklmnopqrstuvwxyz "
!define NUMERIC "1234567890"

RequestExecutionLevel admin

; MUI 1.67 compatible ------
!include "MUI.nsh"
!include "FileFunc.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "..\images\Basex.ico"
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
!insertmacro MUI_PAGE_FINISH

Function run_basex
        nsExec::Exec '$INSTDIR\${PRODUCT_NAME}.exe'
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
# first password field
!insertmacro MUI_INSTALLOPTIONS_READ $R0 "Options" "Field 2" "State"
# second password field
!insertmacro MUI_INSTALLOPTIONS_READ $R1 "Options" "Field 3" "State"
# webport field
!insertmacro MUI_INSTALLOPTIONS_READ $R3 "Options" "Field 11" "State"
# dbpath field
!insertmacro MUI_INSTALLOPTIONS_READ $R4 "Options" "Field 12" "State"
# Admin password modification
${If} $R1 == $R0
  Push "$R1"
  Push "${ALPHA}"
  Call Validate
  Pop $0
  ${If} $0 == 0
    MessageBox MB_OK "Passwords contain invalid characters."
    Abort
  ${ElseIf} $R0 != "admin"
    nsExec::Exec '$INSTDIR\basex.bat -c alter user admin $R0'
  ${EndIf}
${Else}
  MessageBox MB_OK "Passwords do not match."
  Abort
${EndIf}
# Port check
${If} $R2 != "1984"
  Push "$R2"
  Push "${NUMERIC}"
  Call Validate
  Pop $0
  ${If} $0 == 0
    MessageBox MB_OK "PORT contains invalid characters."
    Abort
  ${EndIf}
${EndIf}
# WebPort check
${If} $R3 != "8984"
  Push "$R3"
  Push "${NUMERIC}"
  Call Validate
  Pop $0
  ${If} $0 == 0
    MessageBox MB_OK "WEBPORT contains invalid characters."
    Abort
  ${EndIf}
${EndIf}
CreateDirectory "$INSTDIR\$R4"
FunctionEnd

; Language files
!insertmacro MUI_LANGUAGE "English"
; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; MUI end ------

Name "${PRODUCT_NAME}"
OutFile "Setup.exe"
InstallDir "$PROGRAMFILES\BaseX"
InstallDirRegKey HKLM "${PRODUCT_DIR_REGKEY}" ""
ShowInstDetails show
ShowUnInstDetails show

Section "Hauptgruppe" SEC01
  SetOutPath "$INSTDIR"
  SetOverwrite ifnewer
  File "${PRODUCT_NAME}.exe"
  File "basexserver.bat"
  File "basexserverstop.bat"
  File "basexclient.bat"
  File "basex.bat"
  File "basexrest.bat"
  File "${JAR}"
  File "..\..\license.txt"
  File ".basex"
  File "..\images\BaseX.ico"
  MessageBox MB_OK $R4
  # set dbpath
  nsExec::Exec '$INSTDIR\basex.bat -Wc set dbpath $INSTDIR\$R4'
  # set port
  nsExec::Exec '$INSTDIR\basex.bat -Wc set port $INSTDIR\$R2'
  #set webport
  nsExec::Exec '$INSTDIR\basex.bat -Wc set restport $INSTDIR\$R3'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\BaseX" \
                 "DisplayName" "BaseX"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\BaseX" \
                 "DisplayIcon" "$\"$INSTDIR\BaseX.ico$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\BaseX" \
                 "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
SectionEnd

Section -AdditionalIcons
  !insertmacro MUI_INSTALLOPTIONS_READ $R5 "Options" "Field 9" "State"
  !insertmacro MUI_INSTALLOPTIONS_READ $R6 "Options" "Field 10" "State"
  ${If} $R5 == 1
    CreateShortCut "$DESKTOP\BaseX.lnk" "$INSTDIR\${PRODUCT_NAME}.exe" "" "$INSTDIR\BaseX.ico" 0
  ${EndIf}
  ${If} $R6 == 1
    CreateDirectory "$SMPROGRAMS\BaseX"
    CreateShortCut "$SMPROGRAMS\BaseX\BaseX.lnk" "$INSTDIR\${PRODUCT_NAME}.exe" "" "$INSTDIR\BaseX.ico" 0
    CreateShortCut "$SMPROGRAMS\BaseX\BaseX Server (Start).lnk" "$INSTDIR\basexserver.bat -s" "" "$INSTDIR\BaseX.ico" 0
    CreateShortCut "$SMPROGRAMS\BaseX\BaseX Server (Stop).lnk" "$INSTDIR\basexserverstop.bat" "" "$INSTDIR\BaseX.ico" 0
    CreateShortCut "$SMPROGRAMS\BaseX\BaseXClient.lnk" "$INSTDIR\basexclient.bat" "" "$INSTDIR\BaseX.ico" 0
    CreateShortCut "$SMPROGRAMS\BaseX\BaseX Standalone.lnk" "$INSTDIR\basex.bat" "" "$INSTDIR\BaseX.ico" 0
    CreateShortCut "$SMPROGRAMS\BaseX\BaseX REST.lnk" "$INSTDIR\basexrest.bat" "" "$INSTDIR\BaseX.ico" 0
    WriteINIStr "$SMPROGRAMS\BaseX\Website.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
    CreateShortCut "$SMPROGRAMS\BaseX\Uninstall.lnk" "$INSTDIR\uninst.exe"
  ${EndIf}
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
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
FunctionEnd

Section Uninstall
  Delete "$DESKTOP\BaseX.lnk"
  RMDir /r "$SMPROGRAMS\BaseX"
  RMDir /r "$INSTDIR"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\BaseX"
  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  DeleteRegKey HKLM "${PRODUCT_DIR_REGKEY}"
  SetAutoClose true
SectionEnd

Function Validate
  Push $0
  Push $1
  Push $2
  Push $3 ;value length
  Push $4 ;count 1
  Push $5 ;tmp var 1
  Push $6 ;list length
  Push $7 ;count 2
  Push $8 ;tmp var 2
  Exch 9
  Pop $1 ;list
  Exch 9
  Pop $2 ;value
  StrCpy $0 1
  StrLen $3 $2
  StrLen $6 $1
  StrCpy $4 0
  lbl_loop:
    StrCpy $5 $2 1 $4
    StrCpy $7 0
    lbl_loop2:
      StrCpy $8 $1 1 $7
      StrCmp $5 $8 lbl_loop_next 0
      IntOp $7 $7 + 1
      IntCmp $7 $6 lbl_loop2 lbl_loop2 lbl_error
  lbl_loop_next:
  IntOp $4 $4 + 1
  IntCmp $4 $3 lbl_loop lbl_loop lbl_done
  lbl_error:
  StrCpy $0 0
  lbl_done:
  Pop $6
  Pop $5
  Pop $4
  Pop $3
  Pop $2
  Pop $1
  Exch 2
  Pop $7
  Pop $8
  Exch $0
FunctionEnd
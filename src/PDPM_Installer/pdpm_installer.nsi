; PDPM_Installer.nsi
; 
; Code to check JRE version is based on
; [http://nsis.sourceforge.net/New_installer_with_JRE_check_
;  (includes_fixes_from_'Simple_installer_with_JRE_check'_and_missing_jre.ini)]

;--------------------------------

; Build Version Information
; Format:
; !define BUILD_VERSION "Version-Info-in-Text Build-Number-in-[DD-MM-YY]-format"
; Examples:
; !define BUILD_VERSION "BETA 2.0" 
;    --> Version-Info without Build-Number
; !define BUILD_VERSION "BETA 2.0 [08-08-08]" 
;    --> Version-Info with Build Number
!define BUILD_VERSION "BETA 2.3"

; Required JRE Version
!define JRE_VERSION "1.5"

; The name of the installer
Name "PDPM (${BUILD_VERSION})"

; The file to write
OutFile "pdpm_setup.exe"

; Icon
Icon "pdpm_logo_6.ico"
UninstallIcon "pdpm_logo_6.ico"

; Installer Font
SetFont /LANG=${LANG_ENGLISH} "Arial" 10

; The default installation directory
InstallDir $PROGRAMFILES\PDPM

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\PDPM" "Install_Dir"

BrandingText " "

ShowInstDetails show

; Var InstallJRE
Var JREPath

Var fp
Var fp2

;--------------------------------

; Pages

PageEx license
    LicenseText "Basic terms of use:" "OK, Next >"
    LicenseData "simple-license.txt"
PageExEnd

; Page custom CheckInstalledJRE
Page custom PreviousInstallation
Page directory
Page instfiles


; Uninst Pages

UninstPage uninstConfirm

PageEx Un.components
    PageCallbacks Un.onSelChange
    ; ComponentText "Do you want to keep the PDPM Report files? If yes, Uncheck this checkbox. If the checkbox is checked, all reports are deleted. (Reports are in $INSTDIR)" " " " "
    ComponentText "Do you want to delete all old PDPM Report files also? If yes, please check this checkbox. If you plan to upgrade to a newer version, leave this checkbox unchecked. (Reports are in $INSTDIR)" " " " "
PageExEnd

UninstPage instfiles

;--------------------------------

; The stuff to install
Section "PDPM"

  SectionIn RO
  
  ; Check if already installed
  ; CALL PreviousInstallation

  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  CALL CheckInstalledJRE

  ; Create file Set-Java-Path.bat
  FileOpen $fp "Set-Java-Path.bat" w
  FileWrite $fp "REM This is a generated file$\r$\n"
  FileWrite $fp "$\r$\n"
  FileWrite $fp 'set JAVAEXE="$JREPath"$\r$\n'
  FileWrite $fp "$\r$\n"
  FileClose $fp

  ; Create Version Info file
  FileOpen $fp2 "bld-info.pdp" w
  FileWrite $fp2 "BUILD Version: ${BUILD_VERSION}"
  FileWrite $fp2 "$\r$\n"
  FileClose $fp2

  ; Copy files
  File "pdpm_logo_6.ico"
  File "PDPM_Collector.exe"
  File "pdpm.jar"
  File "Generate-All-Reports.bat"
  File "Generate-Report.bat"
  File "Today's-Realtime-Report.bat"
  File "Yesterday's-Report.bat"
  File "gpl-license.txt"
  File "cfg-pdpm.pdp"
  File "cfg-applications.pdp"

  ; Create folders
  CreateDirectory "$INSTDIR\Logs"
  CreateDirectory "$INSTDIR\Reports"

  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\PDPM "Install_Dir" "$INSTDIR"
  
  ; Write the valid flag into the registry
  WriteRegStr HKLM SOFTWARE\PDPM "Valid" "1"
  
  ; Write the Version into the registry
  WriteRegStr HKLM SOFTWARE\PDPM "Version" "${BUILD_VERSION}"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PDPM" "DisplayName" "PDPM"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PDPM" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PDPM" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PDPM" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
  ; Start Menu Entry
  CreateDirectory "$SMPROGRAMS\PDPM"
  CreateShortCut "$SMPROGRAMS\PDPM\All Reports.lnk" "$INSTDIR\Reports"
  CreateShortCut "$SMPROGRAMS\PDPM\Today's Realtime Report.lnk" "$INSTDIR\Today's-Realtime-Report.bat" "" "$INSTDIR\PDPM_Collector.exe" 0
  CreateShortCut "$SMPROGRAMS\PDPM\Yesterday's Report.lnk" "$INSTDIR\Yesterday's-Report.bat" "" "$INSTDIR\PDPM_Collector.exe" 0
  CreateShortCut "$SMPROGRAMS\PDPM\Edit Applications.lnk" "$WINDIR\notepad.exe" "$INSTDIR\cfg-applications.pdp" "$INSTDIR\PDPM_Collector.exe" 0
  CreateShortCut "$SMPROGRAMS\PDPM\Feedback.lnk" "http://pdpm.sourceforge.net"
  CreateShortCut "$SMPROGRAMS\PDPM\Help.lnk" "http://pdpm.sourceforge.net"
  CreateShortCut "$SMPROGRAMS\PDPM\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0

  ; Startup Folder Entry
  CreateShortCut "$SMSTARTUP\PDPM_Collector.exe.lnk" "$INSTDIR\PDPM_Collector.exe" "" "$INSTDIR\PDPM_Collector.exe" 0

  ; Start PDPM_Collector.exe
  ; Clear Error flag
  ClearErrors
  Exec "$INSTDIR\PDPM_Collector.exe"
  Sleep 500
  IfErrors err_pdpm_collector noerr_end
err_pdpm_collector:
  MessageBox MB_OK "There was an error starting PDPM. Please go to $INSTDIR and double-click on PDPM_Collector.exe to start PDPM. If you face an error, please file a bug report."
noerr_end:
SectionEnd

;--------------------------------

; Uninstaller


; Default Uninstall Section
Section "Un.PDPM Program Files"

  SectionIn RO

  ; Kills PDPM_Collector process
  Processes::KillProcess "PDPM_Collector.exe"
  
  ; Remove files and uninstaller
  Delete "$INSTDIR\Set-Java-Path.bat"
  Delete "$INSTDIR\pdpm_logo_6.ico"
  Delete "$INSTDIR\PDPM_Collector.exe"
  Delete "$INSTDIR\pdpm.jar"
  Delete "$INSTDIR\Generate-All-Reports.bat"
  Delete "$INSTDIR\Generate-Report.bat"
  Delete "$INSTDIR\Today's-Realtime-Report.bat"
  Delete "$INSTDIR\Yesterday's-Report.bat"
  Delete "$INSTDIR\gpl-license.txt"
  Delete "$INSTDIR\bld-info.pdp"
  Delete "$INSTDIR\uninstall.exe"
  Delete "$INSTDIR\cfg-pdpm.pdp"
  Delete "$INSTDIR\cfg-applications.pdp"

  Delete "$INSTDIR\dbg-pdpm-java.log"
  Delete "$INSTDIR\pdpm.log"
  Delete "$INSTDIR\Open-Current-Report.bat"

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\PDPM\*.*"

  ; Remove directories used
  RMDir "$SMPROGRAMS\PDPM"

  ; Remove startup entry
  Delete "$SMSTARTUP\PDPM_Collector.exe.lnk"

  ; Registry keys are not removed, but only the "Valid" flag is reset
  ; So that during upgrade, we can install in the same path

  ; Write the valid flag into the registry
  WriteRegStr HKLM SOFTWARE\PDPM "Valid" "0"
  
  ; DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PDPM"
  ; DeleteRegKey HKLM SOFTWARE\PDPM

SectionEnd

; Reports Section
Section /o "Un.Logs and Reports"
  RMDir /r "$INSTDIR\Logs"
  RMDir /r "$INSTDIR\Reports"
  RMDir "$INSTDIR"
SectionEnd

;--------------------------------

; Functions

Function CheckInstalledJRE
  ; MessageBox MB_OK "PDPM requires Java (JRE ${JRE_VERSION} or greater). Checking Installed JRE Version. Press OK to Continue"
  Push "${JRE_VERSION}"
  Call DetectJRE
  Exch $0
  StrCmp $0 "0" NoFound
  StrCmp $0 "-1" FoundOld
  Goto JREAlreadyInstalled
 
FoundOld:
  ; MessageBox MB_OK "JRE Version is OLD"
  Goto MustInstallJRE
 
NoFound:
  ; MessageBox MB_OK "JRE not found"
  Goto MustInstallJRE
 
MustInstallJRE:
  ; Exch $0	; $0 now has the installoptions page return value
  ; Do something with return value here
  ; Pop $0	; Restore $0
  ; StrCpy $InstallJRE "yes"
  MessageBox MB_OK "Java (${JRE_VERSION} or greater) is not installed. Please install Java first from http://pdpm.sourceforge.net and then install PDPM. Installation will exit now."
  CALL ErrorExit
 
JREAlreadyInstalled:
  Return
 
FunctionEnd
 
; DetectJRE. Version requested is on the stack.
; Returns: 0 - JRE not found. -1 - JRE found but too old.
;          Otherwise - Path to JAVA EXE
 
Function DetectJRE
  Exch $0	; Get version requested  
		; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1	; $1 = Java version string (ie 1.5.0)
  Push $2	; $2 = Javahome
  Push $3	; $3 and $4 are used for checking the major/minor version of java
  Push $4
  !ifdef INSTALLER_DEBUG
  MessageBox MB_OK "Detecting JRE"
  !endif
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  !ifdef INSTALLER_DEBUG
  MessageBox MB_OK "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\CurrentVersion: $1"
  !endif
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  !ifdef INSTALLER_DEBUG
  MessageBox MB_OK "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\$1\JavaHome: $2"
  !endif
  StrCmp $2 "" DetectTry2
  Goto GetJRE
 
DetectTry2:
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  !ifdef INSTALLER_DEBUG
  MessageBox MB_OK "HKLM\SOFTWARE\JavaSoft\Java Development Kit\CurrentVersion: $1"
  !endif
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  !ifdef INSTALLER_DEBUG
  MessageBox MB_OK "HKLM\SOFTWARE\JavaSoft\Java Development Kit\$1\JavaHome: $2"
  !endif
  StrCmp $2 "" NoFound
 
GetJRE:
; $0 = version requested. $1 = version found. $2 = javaHome
  ; MessageBox MB_OK "Getting JRE"
  IfFileExists "$2\bin\java.exe" 0 NoFound
  StrCpy $3 $0 1			; Get major version. Example: $1 = 1.5.0, now $3 = 1
  StrCpy $4 $1 1			; $3 = major version requested, $4 = major version found
  ; MessageBox MB_OK "Want $3 , found $4"
  IntCmp $4 $3 0 FoundOld FoundNew
  StrCpy $3 $0 1 2
  StrCpy $4 $1 1 2			; Same as above. $3 is minor version requested, $4 is minor version installed
  ; MessageBox MB_OK "Want $3 , found $4" 
  IntCmp $4 $3 FoundNew FoundOld FoundNew
 
NoFound:
  ; MessageBox MB_OK "JRE not found"
  Push "0"
  Goto DetectJREEnd
 
FoundOld:
  ; MessageBox MB_OK "JRE too old: $3 is older than $4"
;  Push ${TEMP2}
  Push "-1"
  Goto DetectJREEnd  
FoundNew:
  ; MessageBox MB_OK "JRE is new: $3 is newer than $4"
  Push "$2\bin\java.exe"
  StrCpy $JREPath "$2\bin\java.exe"
;  Push "OK"
;  Return
   Goto DetectJREEnd
DetectJREEnd:
	; Top of stack is return value, then r4,r3,r2,r1
	Exch	; => r4,rv,r3,r2,r1,r0
	Pop $4	; => rv,r3,r2,r1r,r0
	Exch	; => r3,rv,r2,r1,r0
	Pop $3	; => rv,r2,r1,r0
	Exch 	; => r2,rv,r1,r0
	Pop $2	; => rv,r1,r0
	Exch	; => r1,rv,r0
	Pop $1	; => rv,r0
	Exch	; => r0,rv
	Pop $0	; => rv 
FunctionEnd

Function ErrorExit
    Quit
FunctionEnd

; Function to hide the default unistall section and show choice for user
; only to delete/do not delete Reports folder during uninstallation
Function Un.onSelChange
    ; Reports folder
    SectionSetText 0 ""
FunctionEnd

Function PreviousInstallation
  ; Check if Valid flag exists
  ReadRegStr $1 HKLM "SOFTWARE\PDPM" "Valid"
  ; If it exists, use it to check if PDPM is already installed
  ; If it does not exist, check based on "Install_Dir" key
  StrCmp $1 "" CheckOldRegKey
  ; Valid=0 means PDPM was installed and has now been uninstalled, so OK
  ; Valid=1 means, PDPM is installed and being used, so NOT OK
  StrCmp $1 "0" PreviousInstallationOK
  GOTO PreviousInstallationNOTOK

CheckOldRegKey:
  ; Old Reg Key - "Valid" flag is not in registry
  ; Check if already installed based on if Install_Dir exists
  ReadRegStr $1 HKLM "SOFTWARE\PDPM" "Install_Dir"
  StrCmp $1 "" PreviousInstallationOK
  GOTO PreviousInstallationNOTOK

PreviousInstallationNOTOK:
  ; Previous Installation Exists
  MessageBox MB_OK|MB_ICONEXCLAMATION "PDPM is already installed. Please uninstall the existing installation first (Start Menu -> PDPM -> Uninstall) and run the installer again to upgrade. Please remember to leave the 'Logs and Reports' checkbox unchecked during uninstallation to retain existing Reports. Installation will exit now."
  CALL ErrorExit

PreviousInstallationOK:
  return

FunctionEnd ;PreviousInstallation

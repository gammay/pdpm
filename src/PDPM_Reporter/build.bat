@ECHO off

SET JAVAC_PATH="D:\Program Files\Java\jdk1.5.0_16\bin"
SET CLASSES_OUTPUT_DIR=classes
SET JAR_FILE_NAME=pdpm.jar

IF "%1" == "" GOTO BUILD_ALL:
IF "%1" == "clean" GOTO CLEAN:
IF "%1" == "/?" GOTO USAGE:
ECHO Invalid Argument: %1
GOTO END:

:USAGE:
ECHO build.bat [clean]
ECHO   No arguments = build all
GOTO END:

:CLEAN:
ECHO Clean...
rd /s /q %CLASSES_OUTPUT_DIR%
del /q %JAR_FILE_NAME%
GOTO END:

:BUILD_ALL:
ECHO Building...
IF NOT EXIST classes mkdir classes

%JAVAC_PATH%\javac -d %CLASSES_OUTPUT_DIR% *.java
SET ERR=%ERRORLEVEL%
REM echo javac returned %ERR%
IF NOT %ERR% EQU 0 GOTO :BuildError

%JAVAC_PATH%\jar cvf %JAR_FILE_NAME% -C %CLASSES_OUTPUT_DIR% .
SET ERR=%ERRORLEVEL%
REM echo javac returned %ERR%
IF NOT %ERR% EQU 0 GOTO :BuildError

GOTO END:

:BuildError
echo.
echo.
echo Build Failed!!! 
echo.
GOTO :END

:END:
PAUSE



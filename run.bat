@echo off
setlocal

echo =============================================================
echo   Starting Student Database Management System
echo   JDBC Implementation with Oracle Database
echo =============================================================

set JAVA_DIR=C:\Program Files\Java\jdk-26.0.1
if exist "%JAVA_DIR%\bin\java.exe" (
    set "JAVA_CMD=%JAVA_DIR%\bin\java.exe"
    set "JAVAC_CMD=%JAVA_DIR%\bin\javac.exe"
) else (
    set "JAVA_CMD=java"
    set "JAVAC_CMD=javac"
)

if not exist "bin\com\student\Main.class" (
    echo Compiling project first...
    if not exist bin mkdir bin
    "%JAVAC_CMD%" -cp "lib/ojdbc6.jar" -d bin src/com/student/model/*.java src/com/student/dao/*.java src/com/student/util/*.java src/com/student/server/*.java src/com/student/*.java
    if %ERRORLEVEL% neq 0 (
        echo Compilation failed! Exiting.
        pause
        exit /b 1
    )
)

echo.
echo Launching Web Server on http://localhost:5000 ...
echo (Press Ctrl+C in this console window anytime to stop the server)
echo.

start "" http://localhost:5000

"%JAVA_CMD%" -cp "bin;lib/ojdbc6.jar" com.student.Main

pause

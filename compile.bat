@echo off
setlocal

echo =============================================================
echo   Compiling Student DBMS Project (JDBC + Oracle)
echo =============================================================

set JAVA_DIR=C:\Program Files\Java\jdk-26.0.1
if exist "%JAVA_DIR%\bin\javac.exe" (
    set "JAVAC=%JAVA_DIR%\bin\javac.exe"
) else (
    set "JAVAC=javac"
)

if not exist bin mkdir bin

echo Compiling Java source files...
"%JAVAC%" -cp "lib/ojdbc6.jar" -d bin src/com/student/model/*.java src/com/student/dao/*.java src/com/student/util/*.java src/com/student/server/*.java src/com/student/*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] Compilation finished successfully! Class files generated in bin/
) else (
    echo.
    echo [ERROR] Compilation failed. Please check the error messages above.
)

echo =============================================================
pause

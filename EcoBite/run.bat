@echo off
REM ============================================
REM ECOBITE - Windows run script
REM Pehle DB.java mein apna MySQL password set karo!
REM ============================================
javac -cp "lib\mysql-connector-j-8.4.0.jar" -d out src\*.java
if errorlevel 1 (
    echo.
    echo COMPILE FAIL - error upar dekho
    pause
    exit /b
)
java -cp "out;lib\mysql-connector-j-8.4.0.jar" EcoBite
pause

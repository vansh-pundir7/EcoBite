@echo off
cd /d "%~dp0"
if not exist db.properties (
    echo Create db.properties from db.properties.example and set your MySQL details.
    pause
    exit /b 1
)
if not exist out mkdir out
javac -cp "lib\mysql-connector-j-8.4.0.jar" -d out src\*.java
if errorlevel 1 (
    echo.
    echo Compile failed. Fix the errors above.
    pause
    exit /b 1
)
java -cp "out;lib\mysql-connector-j-8.4.0.jar" EcoBite

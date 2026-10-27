@echo off
cd /d "%~dp0.."
if not exist out mkdir out
javac --release 17 -cp "lib\mysql-connector-j-8.4.0.jar" -d out src\*.java test\SmokeTest.java
if errorlevel 1 exit /b 1
java -cp "out;lib\mysql-connector-j-8.4.0.jar" SmokeTest

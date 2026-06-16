@echo off
cd /d "%~dp0"
echo [BUILD] Compiling...
javac -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" Main.java
if %errorlevel% neq 0 (
    echo [BUILD] Compile failed!
    pause
    exit /b 1
)
echo [BUILD] Compile OK. Launching...
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" Main
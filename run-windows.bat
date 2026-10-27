@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo EduVerify India - APAAR Demo Prototype
echo ========================================
where java >nul 2>nul
if errorlevel 1 (echo ERROR: Install Java 17+ from https://adoptium.net/ & pause & exit /b 1)
where mvn >nul 2>nul
if errorlevel 1 (echo ERROR: Maven is not available in PATH. Install Maven and restart VS Code. & pause & exit /b 1)
echo Starting prototype. Keep this window open.
call mvn spring-boot:run
pause

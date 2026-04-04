@echo off
title PulsE Project Initializer
cd /d "%~dp0"

powershell -NoProfile -ExecutionPolicy Bypass -File "./init-project.ps1"

echo.
echo Press any key to exit...
pause >nul
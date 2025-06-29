@echo off
call mvnw.cmd clean compile
if %ERRORLEVEL% NEQ 0 (
    echo Build failed with error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)
echo Build completed successfully

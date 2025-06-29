@echo off
echo Verifying Maven build...
call mvnw.cmd --version
if %ERRORLEVEL% NEQ 0 (
    echo Failed to verify Maven installation
    exit /b %ERRORLEVEL%
)

echo.
echo Running Maven clean compile...
call mvnw.cmd clean compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo Build failed with error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)

echo.
echo Build completed successfully!

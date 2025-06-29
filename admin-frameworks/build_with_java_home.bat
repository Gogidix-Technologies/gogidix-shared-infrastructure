@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.15.6-hotspot
echo JAVA_HOME set to: %JAVA_HOME%
echo.
echo Current directory: %CD%
echo.
echo Running Maven build...
call mvnw.cmd clean compile test package
if %ERRORLEVEL% NEQ 0 (
    echo Build failed with error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)
echo.
echo Build completed successfully!

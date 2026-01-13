@echo off
echo ========================================
echo Running Simple Redis Replication Demo
echo ========================================
echo.

cd /d "%~dp0"
javac -d target/classes -sourcepath src/main/java src/main/java/com/architecture/principle/SimpleReplicationDemo.java
if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo.
    java -cp target/classes com.architecture.principle.SimpleReplicationDemo
) else (
    echo Compilation failed!
    pause
)

echo.
echo ========================================
pause

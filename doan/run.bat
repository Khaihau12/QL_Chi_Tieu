@echo off
cd /d "%~dp0"
echo Running JavaFX Application...
java --module-path "%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\13;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\13;%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\13;target\classes" --add-modules javafx.controls -cp "target\classes;%USERPROFILE%\.m2\repository\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar" com.example.App
pause

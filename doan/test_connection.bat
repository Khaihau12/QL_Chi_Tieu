@echo off
echo ================================================
echo KIEM TRA KET NOI DATABASE
echo ================================================
echo.

REM Compile the Java classes
echo [1] Compiling Java classes...
javac -cp ".;lib/*" -d target/classes src/main/java/com/example/util/DatabaseConnection.java src/main/java/com/example/dao/DanhMucDAO.java src/main/java/com/example/model/DanhMuc.java
echo.

REM Test database connection
echo [2] Testing database connection...
java -cp "target/classes;lib/*" com.example.dao.DanhMucDAO
echo.

pause

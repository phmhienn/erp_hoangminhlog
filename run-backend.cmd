@echo off
REM Chay backend khong can cai Maven: Maven Wrapper tu tai Maven ve lan dau.
REM Chi can may co JDK 17 tro len.
cd /d "%~dp0backend"
call "%~dp0backend\mvnw.cmd" spring-boot:run %*

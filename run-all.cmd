@echo off
setlocal
if /i "%~1"=="backend" goto backend
if /i "%~1"=="frontend" goto frontend

where java.exe >nul 2>&1
if errorlevel 1 (
  echo Khong tim thay Java. Hay cai JDK 21 va cau hinh JAVA_HOME/PATH.
  goto failed
)
where npm.cmd >nul 2>&1
if errorlevel 1 (
  echo Khong tim thay npm. Hay cai Node.js va mo lai file nay.
  goto failed
)
if not exist "%~dp0backend\mvnw.cmd" (
  echo Khong tim thay backend\mvnw.cmd.
  goto failed
)
if not exist "%~dp0frontend\package.json" (
  echo Khong tim thay frontend\package.json.
  goto failed
)

start "ERP Backend" "%ComSpec%" /k ""%~f0" backend"
start "ERP Frontend" "%ComSpec%" /k ""%~f0" frontend"
echo Da mo hai cua so chay backend va frontend.
echo Mo URL Local do Vite hien thi trong cua so ERP Frontend.
echo Nhan Ctrl+C trong tung cua so de dung chuong trinh.
exit /b 0

:backend
cd /d "%~dp0backend"
if errorlevel 1 goto failed
echo Dang chay backend. Su dung cau hinh .env hien co; can JDK 21.
call mvnw.cmd spring-boot:run
exit /b %errorlevel%

:frontend
cd /d "%~dp0frontend"
if errorlevel 1 goto failed
if not exist "node_modules\vite\bin\vite.js" (
  echo Dang cai thu vien frontend...
  call npm.cmd install
  if errorlevel 1 goto failed
)
call npm.cmd run dev
exit /b %errorlevel%

:failed
echo Khong the khoi dong. Kiem tra thong bao ben tren.
pause
exit /b 1

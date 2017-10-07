

pushd %~dp0
set current_dir=%CD%
popd

set runnerPath=%1
set autPath=%2
set project=%current_dir%

set testResults=%runnerPath%\results
set runnerWorkspace=%testResults%\runner-workspace
set autWorkspace=%testResults%\aut-workspace-
set autOut=%testResults%\aut-out-
set junitReport=%testResults%\results.xml
set htmlReport=%testResults%\results.html

rmdir /q /s %testResults%
mkdir %testResults%

java -jar %runnerPath%\plugins\org.eclipse.equinox.launcher_1.3.200.v20160318-1642.jar ^
    -application org.eclipse.rcptt.runner.headless ^
    -data %runnerWorkspace% ^
    -aut %autPath% ^
    -autWsPrefix %autWorkspace% ^
    -autConsolePrefix %autOut% ^
    -htmlReport %htmlReport% ^
    -junitReport %junitReport% ^
    -import %project%

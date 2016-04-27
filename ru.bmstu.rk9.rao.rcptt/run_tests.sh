#!/bin/bash

current_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

runnerPath=$1
autPath=$2
project=$current_dir

testResults=$runnerPath/results
runnerWorkspace=$testResults/runner-workspace
autWorkspace=$testResults/aut-workspace-
autOut=$testResults/aut-out-
junitReport=$testResults/results.xml
htmlReport=$testResults/results.html

rm -rf $testResults
mkdir $testResults

java -jar $runnerPath/plugins/org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar \
    -application org.eclipse.rcptt.runner.headless \
    -data $runnerWorkspace \
    -aut $autPath \
    -autWsPrefix $autWorkspace \
    -autConsolePrefix $autOut \
    -htmlReport $htmlReport \
    -junitReport $junitReport \
    -import $project

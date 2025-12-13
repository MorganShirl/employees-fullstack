# set-java25.ps1
$env:JAVA_HOME="D:\My_Documents\Work\Dev\Java\Installed\_jdk\jdk-temurin-25.0.1_8"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
Write-Host "Switched to Java 25"

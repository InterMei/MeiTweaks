@rem Minimal Gradle wrapper launcher for Windows.
@echo off
set DIR=%~dp0
set CLASSPATH=%DIR%gradle\wrapper\gradle-wrapper.jar
"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

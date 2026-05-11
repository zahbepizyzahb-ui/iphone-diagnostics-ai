@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows: gradle.bat
@rem
@rem ##########################################################################

setlocal enabledelayedexpansion

set DEFAULT_JVM_OPTS=" -Xmx64m" "-Xms64m"

if "%JAVA_HOME%"==\"\" (
    echo Error: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    echo.
    echo Please set the JAVA_HOME variable in your environment to match the
    echo location of your Java installation.
    goto fail
)

for /f "tokens=*" %%i in ('findstr /R "^distributionUrl=" gradle\wrapper\gradle-wrapper.properties') do set DOWNLOAD_URL=%%i
for /f "tokens=1* delims==" %%a in ("%DOWNLOAD_URL%") do set DOWNLOAD_URL=%%b

if exist "%GRADLE_USER_HOME%\wrapper\dists\gradle-8.4\bin\gradle.bat" (
    if "%GRADLE_OFFLINE%"==\"1\" (
        echo.
        echo Gradle already downloaded. Skipping download.
        echo.
    ) else (
        echo Downloading Gradle...
    )
) else (
    if not exist "%GRADLE_USER_HOME%\wrapper\dists" mkdir "%GRADLE_USER_HOME%\wrapper\dists"
)

echo.
echo Executing: gradle.bat %*
echo.

"%JAVA_HOME%\bin\java.exe" %DEFAULT_JVM_OPTS% -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

if %ERRORLEVEL% neq 0 goto fail

goto end

:fail
rem Set variable GRADLE_WRAPPER_ERROR in your environment in order to set a custom error message when this occurs.
if "%GRADLE_WRAPPER_ERROR%"==\"\" (
    echo Gradle exited with error code %ERRORLEVEL%
) else (
    echo %GRADLE_WRAPPER_ERROR%
)
exit /b %ERRORLEVEL%

:end
endlocal

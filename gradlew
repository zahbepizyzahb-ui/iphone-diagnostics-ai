#!/usr/bin/env bash
# Simplified Gradle wrapper script for CI
java -Dfile.encoding=UTF-8 -Xmx2g -classpath "gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"

# Triggering new build run

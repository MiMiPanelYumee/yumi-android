#!/usr/bin/env sh
# Minimal gradlew script for CI
exec ./gradle/wrapper/gradle-wrapper.jar "$@"
# Note: GitHub Actions setup-java/setup-gradle can handle missing wrapper jar
# if configured, but a real wrapper jar is binary. We rely on CI tools to bootstrap it.

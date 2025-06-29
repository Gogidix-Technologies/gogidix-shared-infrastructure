#!/bin/bash
case "$1" in
    "test") mvn test ;;
    "build") mvn clean package ;;
    "run") mvn spring-boot:run ;;
    *) echo "Usage: $0 {test|build|run}" ;;
esac

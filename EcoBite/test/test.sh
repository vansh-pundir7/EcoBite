#!/bin/sh
cd "$(dirname "$0")/.."
mkdir -p out
javac --release 17 -cp "lib/mysql-connector-j-8.4.0.jar" -d out src/*.java test/SmokeTest.java || exit 1
java -cp "out:lib/mysql-connector-j-8.4.0.jar" SmokeTest

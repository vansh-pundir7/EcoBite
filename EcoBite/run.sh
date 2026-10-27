#!/bin/bash
cd "$(dirname "$0")"
if [ ! -f db.properties ]; then
    echo "Create db.properties from db.properties.example and set your MySQL details."
    exit 1
fi
mkdir -p out
javac -cp "lib/mysql-connector-j-8.4.0.jar" -d out src/*.java || exit 1
java -cp "out:lib/mysql-connector-j-8.4.0.jar" EcoBite

#!/bin/bash
# ============================================
# ECOBITE - Linux/Mac run script
# Pehle DB.java mein apna MySQL password set karo!
# ============================================
javac -cp "lib/mysql-connector-j-8.4.0.jar" -d out src/*.java || exit 1
java -cp "out:lib/mysql-connector-j-8.4.0.jar" EcoBite

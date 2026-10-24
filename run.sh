#!/bin/sh
set -eu
cd "$(dirname "$0")"

echo "============================================================="
echo "   Compiling and Starting Student DBMS Portal (JDBC)"
echo "============================================================="

mkdir -p bin
javac -cp "lib/ojdbc6.jar" -d bin src/com/student/model/*.java src/com/student/dao/*.java src/com/student/util/*.java src/com/student/server/*.java src/com/student/*.java

echo "Starting server on http://localhost:5000 ..."
java -cp "bin:lib/ojdbc6.jar" com.student.Main

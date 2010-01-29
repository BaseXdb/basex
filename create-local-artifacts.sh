#!/bin/bash
mvn install:install-file -Dfile=src/main/resources/xqjapi.jar -DgroupId=xqj -DartifactId=xqj-api  -Dversion=1 -Dpackaging=jar -DgeneratePom=true
# locally install xmldb
mvn install:install-file -Dfile=src/main/resources/xmldb.jar -DgroupId=xmldb -DartifactId=xmldb-api  -Dversion=1 -Dpackaging=jar -DgeneratePom=true

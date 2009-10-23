#!/bin/sh

# BaseX classpath
cp="../bin"
img="./images/BaseX.icns"

java -Xdock:name="BaseX" -Xdock:icon="${img}" -cp ${cp} org.basex.BaseX

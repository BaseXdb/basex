#!/bin/sh

if [ -z "$BASEX_PATH" ] 
then
	echo "Environment variable BASEX_PATH is not set."
	BASEX_PATH="."
fi

# BaseX classpath
cp="$BASEX_PATH/bin"
img="$BASEX_PATH/images/BaseX.icns"

java -Xdock:name="BaseX" -Xdock:icon="${img}" -cp ${cp} org.basex.BaseXGUI $@

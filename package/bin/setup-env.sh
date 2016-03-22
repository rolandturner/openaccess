#!/bin/sh

OPENACCESS_HOME="$(pwd)"
if [ "$(basename $OPENACCESS_HOME)" = "bin" ]; then
	OPENACCESS_HOME="$(dirname $OPENACCESS_HOME)"
fi

alljars(){
    for i in $*; do
        echo -n "`find $i -name "*.jar" | tr "\n" ":" `"
    done
}

classpath(){
    alljars $* | sed -e "s/:$//"; echo
}

CLASSPATH=`classpath $OPENACCESS_HOME/lib `:$OPENACCESS_HOME/license:$CLASSPATH
PATH="$OPENACCESS_HOME/bin:${PATH}"

export CLASSPATH PATH
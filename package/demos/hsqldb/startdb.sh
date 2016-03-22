#!/bin/sh
#################################################################################
#
#   HSQLDB startup script for Unix and Mac OS-X
#
#################################################################################

darwin=false;
case "`uname`" in
    Darwin*) darwin=true;
esac

if [ -z "$JAVA_HOME" ]; then
    if $darwin ; then
        JAVA_HOME=/System/Library/Frameworks/JavaVM.Framework/Versions/CurrentJDK/Home
    else
        JAVA_HOME=/usr/java/jdk1.3.1_02
    fi
fi

#use the OPENACCESS_HOME if available else guess the default
if [ -z "$OPENACCESS_HOME" ]; then
    OPENACCESS_HOME=../..
fi

MAIN_CLASS_NAME="org.hsqldb.Server"
JVM_ARGS="-ms16M -mx64M"

cd `dirname "$0"`
CLASSPATH=$OPENACCESS_HOME/lib/hsqldb.jar
                 
exec ${JAVA_HOME}/bin/java -cp $CLASSPATH $JVM_ARGS $MAIN_CLASS_NAME


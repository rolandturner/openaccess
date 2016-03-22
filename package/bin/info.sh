#!/bin/sh
#################################################################################
#
#   Workbench startup script for Unix and Mac OS-X
#
#################################################################################
#
#   Please customize this script by specifying locations of JDK_HOME and
#   OPENACCESS_HOME below
#
#################################################################################
#
#   Specify the JAVA_HOME for this script. JAVA_HOME should refer to the
#   home location where your system's Java Development Kit is installed
#   For instance, the supplied example assumes the JDK is installed at
#   /usr/java/j2sdk1.3.1_02
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
export JAVA_HOME

#################################################################################
#
#   Ensure the OPENACCESS_HOME var for this script points to the
#   home directory where JDO Genie is installed on your system.
#
#################################################################################

cd `dirname "$0"`
cd ..
OPENACCESS_HOME=`pwd`
export OPENACCESS_HOME

MAIN_CLASS_NAME="com.versant.core.jdo.license.Info"
JVM_ARGS="-ms16M -mx64M -DOPENACCESS_HOME=$OPENACCESS_HOME"

while [ $# -gt 0 ]; do
    args="$args $1"
    shift
done

oldClasspath=$CLASSPATH

alljars(){
    for i in $*; do
        echo -n "`find $i -name "*.jar" | tr "\n" ":" `"
    done
}

classpath(){
    alljars $* | sed -e "s/:$//"; echo
}

CLASSPATH=`classpath ./lib  ./tools`:./license

#################################################################################
#
#  Append old classpath to current classpath
#
#################################################################################

if [ ! -z "$oldClasspath" ]; then
    CLASSPATH=${CLASSPATH}:$oldClasspath
fi

export CLASSPATH

exec ${JAVA_HOME}/bin/java $JVM_ARGS $MAIN_CLASS_NAME $args

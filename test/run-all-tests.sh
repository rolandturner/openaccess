# Run all of the tests in alltests.txt using "ant junit-run-clean". Any
# extra args are passed to ant.

(while read a b; do
    if [ "" == "$a" ] ; then
        continue
    fi
    if [ "#" == "$a" ] ; then
        continue
    fi
    echo "==== ant junit-run-all $a $*"
    if ! ant junit-run-clean -Dtest=$a "$*" ; then
        exit 1
    fi
done
) < alltests.txt

#!/bin/sh
# Start a Hypersonic server running databases for all the demo
cd `dirname "$0"`
cd ..
sh demos/hsqldb/startdb.sh

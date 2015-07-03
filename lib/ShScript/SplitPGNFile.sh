#!/bin/bash
#include <stdio.h>

T=$(date +%R)

if [ -z "$1" ]||[ -d "$1" ]
then
	echo "USAGE : sh run.sh filename.pgn"
	exit 1
else
	FILEPATH=$1
	FILENAME=$(basename $1)
fi

FILESIZE=`du -k "$FILEPATH" | cut -f1`

echo "$T File $FILEPATH = $FILESIZE Ko"

if [ -f "$FILEPATH" ]; then
	rm -rf resources/tmp/*
	if [ "$FILESIZE" -gt "25000" ]; then
		echo "$T Split into small files"
		mawk '/\[Event / { if(++delim % 100000 == 0) { next }} { file = sprintf("resources/tmp/chunk%s", int(delim / 100000)); print >> file;}' < $FILEPATH
		FILEPATH=""
	fi
	#echo "$T Exec MainClass java class"
	#javac -cp src src/pgnparse/*.java
	#java -cp mysql-connector-java-5.1.35-bin.jar:src pgnparse/PgnToSql $FILEPATH
	#rm -rf tmp/*
fi

exit 0

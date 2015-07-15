#!/bin/bash

LOCKFILE=$HOME/.chessanalyse/lock

cnt=$HOME/.chessanalyse/cnt

create_lock_or_wait () {
  path="$LOCKFILE"
  wait_time="${2:-1}"
  while true; do
        if mkdir "${path}.lock.d"; then
           break;
        fi
        sleep $wait_time
  done
}

remove_lock () {
  path="$LOCKFILE"
  rmdir "${path}.lock.d"
}

analyse_file () {
  create_lock_or_wait
  if [ -e ${cnt} ] ; then
	count=$(cat ${cnt})
  else
	count=-1
  fi
  ((count++))
  echo ${count} > ${cnt}
  remove_lock
  /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java -jar run.jar ${count}
}


while [[ ${count} -lt 9010 ]] ; do
	analyse_file
	((count++))
	echo ${count}
done


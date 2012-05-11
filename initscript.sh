#!/bin/sh

PATH=/bin:/usr/bin:/sbin:/usr/sbin
DAEMON=/a/uploadchallenge/current/run.sh
PIDDIR=/a/uploadchallenge/current/tmp/pids
PIDFILE="$PIDDIR/uploadchallenge.pid"
USER="linkflow"

export PATH="${PATH:+$PATH:}/usr/sbin:/sbin"

if [ ! -d $PIDDIR ]; then
  mkdir $PIDDIR
  chown servicerunner.servicerunner $PIDDIR
fi

case "$1" in
  start)
    start-stop-daemon --start --quiet --oknodo --background --pidfile $PIDFILE --exec $DAEMON --chuid $USER -- $SERVICES_OPTS
    sleep 5
    $0 status
  ;;

  stop)
    if start-stop-daemon --stop --quiet --pidfile $PIDFILE; then
      exit 0
    else
      exit 1
    fi
  ;;
esac
#!/bin/bash
#  jar name
APP_NAME="hss-core-1.0.0-RELEASE.jar"
PROFILES="--spring.profiles.active=test"

#helper
usage() {
    echo "Usage: sh hss.sh [start|stop|restart|status]"
    exit 1
}
 
#check is run 
is_exist(){
  pid=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}' `
  #run:1 not:0
  if [ -z "${pid}" ]; then
   return 1
  else
    return 0
  fi
}
 
#start func
start(){
  is_exist
  if [ $? -eq "0" ]; then
    echo "${APP_NAME} is already running. pid=${pid}"
  else
    nohup java -jar $APP_NAME $PROFILES &
	echo "${APP_NAME} is starting ..."
	tail -f nohup.out
    #nohup java -jar $APP_NAME > /dev/null 2>&1 &
  fi
}
 
#stop func
stop(){
  is_exist
  if [ $? -eq "0" ]; then
    kill -9 $pid
	echo "${APP_NAME} has stopped"
  else
    echo "${APP_NAME} is not running"
  fi  
}
 
#run status
status(){
  is_exist
  if [ $? -eq "0" ]; then
    echo "${APP_NAME} is running. pid is ${pid}"
  else
    echo "${APP_NAME} is not running"
  fi
}
 
#restart func
restart(){
  stop
  start
}
 
#choose func
case "$1" in
  "start")
    start
    ;;
  "stop")
    stop
    ;;
  "status")
    status
    ;;
  "restart")
    restart
    ;;
  *)
    usage
    ;;
esac


#!/bin/bash

# 指定java命令的路径和jar包的路径
JAR_FILE=chatgptForJava.jar

# 检查程序是否已经启动
pid=`ps -ef | grep $JAR_FILE | grep -v grep | awk '{print $2}'`
if [ -n "$pid" ]; then
	echo "Stopping process id: $pid"
	kill -9 $pid
	echo "The application has stopped."
    exit 1
fi

echo "The application is not running."

#!/bin/bash

# 指定java命令的路径和jar包的路径
JAR_FILE=chatgptForJava.jar

# 检查程序是否已经启动
echo "Starting application ..."
pid=`ps -ef | grep $JAR_FILE | grep -v grep | awk '{print $2}'`
if [ -n "$pid" ]; then
    echo "The application is already running. PID is $pid."
    exit 1
fi
# 启动程序
current_dir=$(cd $(dirname $0); pwd)
cd $current_dir/..
nohup java -jar $JAR_FILE >/dev/null 2>&1 &
echo "The application has successfully started."

# FROM: 基础镜像，基于jdk8镜像,因为我系统用的是armv7架构，所以用这个基础镜像包
FROM mrl111/armv7-jdk8-cn:1.0

#添加文件
ADD /home/runner/work/chatgptForJava/chatgptForJava/target/*.jar /chatGPT/app.jar
ADD /home/runner/work/chatgptForJava/chatgptForJava/target/config /chatGPT/config
ADD /home/runner/work/chatgptForJava/chatgptForJava/target/lib /chatGPT/lib
RUN chmod u+x /chatGPT/app.jar

# EXPOSE：声明端口
EXPOSE 9389

#jvm参数
ENV JAVA_OPTS=""

# 指定工作目录到程序目录，这样生成的日志会在程序目录
WORKDIR /chatGPT
# ENTRYPOINT：docker启动时，运行的命令，这里容器启动时直接运行jar服务。
ENTRYPOINT java ${JAVA_OPTS} -Dfile.encoding=utf-8 -jar app.jar
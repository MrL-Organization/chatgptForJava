# FROM: 基础镜像，基于jdk8镜像开始 因为我系统用的是armv7架构，所以用这个基础镜像包
#FROM java:8-alpine
FROM armv7/armhf-java8

# COPY: 将应用的配置文件也拷贝到镜像中。
COPY *.jar /chatGPT/app.jar
COPY config /chatGPT/config
COPY lib /chatGPT/lib

# EXPOSE：声明端口
EXPOSE 9389

ENTRYPOINT ["java","-jar","/chatGPT/app.jar"]
# ENTRYPOINT：docker启动时，运行的命令，这里容器启动时直接运行jar服务。
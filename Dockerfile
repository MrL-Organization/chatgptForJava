# 二开推荐阅读[如何提高项目构建效率](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/scene/build/speed.html)
# 选择构建用基础镜像。如需更换，请到[dockerhub官方仓库](https://hub.docker.com/_/java?tab=tags)自行选择后替换。
FROM arm32v7/maven:3-jdk-8-slim as build

# 指定构建过程中的工作目录
WORKDIR /app

# 将src目录下所有文件，拷贝到工作目录中src目录下（.gitignore/.dockerignore中文件除外）
COPY src /app/src

# 将pom.xml文件，拷贝到工作目录下
COPY settings.xml pom.xml /app/

# 执行代码编译命令
# 自定义settings.xml, 选用国内镜像源以提高下载速度
RUN mvn -s /app/settings.xml -f /app/pom.xml clean package

# FROM: 基础镜像，基于jdk8镜像,因为我系统用的是armv7架构，所以用这个基础镜像包
FROM mrl111/armv7-jdk8-cn:1.0

# COPY: 将应用的配置文件也拷贝到镜像中。
COPY --from=build /app/target/*.jar /chatGPT/app.jar
COPY --from=build /app/target/config /chatGPT/config
COPY --from=build /app/target/lib /chatGPT/lib

# EXPOSE：声明端口
EXPOSE 9389

#jvm参数
ENV JAVA_OPTS=""

# 指定工作目录到程序目录，这样生成的日志会在程序目录
WORKDIR /chatGPT
# ENTRYPOINT：docker启动时，运行的命令，这里容器启动时直接运行jar服务。
ENTRYPOINT java ${JAVA_OPTS} -Dfile.encoding=utf-8 -jar app.jar
FROM openjdk:21
#  ./target/demo-0.0.1-SNAPSHOT.jar 为编译后的 jar 包位置
# jar 包的名称要与自己配置的相同，格式为：项目名称-版本号.jar
COPY ./target/mqtt-demo-1.0.jar app.jar
EXPOSE 16692
ENTRYPOINT ["java","-jar"]
CMD ["app.jar"]
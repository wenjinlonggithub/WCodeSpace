FROM openjdk:11.0.12-jre-slim

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone


COPY target/*.jar hos-gateway.jar

ENV AGENT_OPTS=""
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $AGENT_OPTS -jar $JAVA_OPTS /hos-gateway.jar"]

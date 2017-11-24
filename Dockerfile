FROM openjdk:8u151-jre-slim
MAINTAINER Olaf Bergner <olaf.bergner@gmx.de>

ARG version
ARG port

WORKDIR /app

COPY target/uberjar/github-api-client-${version}-standalone.jar /app/github-api-client-${version}-standalone.jar

ENV PORT ${port}
ENV GH_API_URL https://api.github.com/graphql
ENV GH_API_TOKEN DUMMY

EXPOSE ${port}

CMD ["java", "-jar", "/app/github-api-client-${version}-standalone.jar"]
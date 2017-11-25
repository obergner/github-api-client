FROM openjdk:8u131-jre-alpine
MAINTAINER Olaf Bergner <olaf.bergner@gmx.de>

ARG version
ARG port

WORKDIR /app

COPY target/uberjar/github-api-client-${version}-standalone.jar /app/github-api-client-${version}-standalone.jar
RUN ln -s /app/github-api-client-${version}-standalone.jar /app/app.jar

ENV PORT ${port}
ENV GH_API_URL https://api.github.com/graphql
ENV GH_API_TOKEN DUMMY

EXPOSE ${port}

CMD ["java", "-jar", "/app/app.jar"]
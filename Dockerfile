FROM openjdk:8u151-jre-slim
MAINTAINER Olaf Bergner <olaf.bergner@gmx.de>

ARG version
ARG port

WORKDIR /app

COPY target/uberjar/github-api-client-${version}-standalone.jar /app/github-api-client-${version}-standalone.jar
RUN ln -s /app/github-api-client-${version}-standalone.jar /app/app.jar

ENV PORT ${port}
ENV LOG_INTERVAL_MS 60000
ENV GH_API_URL https://api.github.com/graphql
ENV GH_API_TOKEN DUMMY
ENV GH_ORG obergner
ENV GH_REPO camelpe
ENV GH_PRS_LAST 3
ENV ROCKSDB_PATH /app/db
ENV MANAGEMENT_API_PORT 3100

EXPOSE ${port}

CMD ["java", "-jar", "/app/app.jar"]

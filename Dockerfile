FROM clojure:lein-2.8.1-alpine

WORKDIR /app

ARG username
ARG password

ENV USERNAME=$username
ENV PASSWORD=$password

COPY ./project.clj .
COPY ./src ./src
COPY ./resources ./resources

RUN lein ring uberjar

ENTRYPOINT java -jar ./target/uberjar/beehive-database-*-standalone.jar


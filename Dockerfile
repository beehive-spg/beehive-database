FROM clojure:lein-2.8.1-alpine as build

WORKDIR /app

ARG username
ARG password

ENV USERNAME=$username
ENV PASSWORD=$password

COPY ./project.clj .
COPY ./src ./src
COPY ./resources ./resources

RUN lein uberjar


FROM openjdk:jre-alpine

COPY --from=build /app/target/beehive-database-*-standalone.jar /database.jar

ENTRYPOINT java -jar ./database.jar

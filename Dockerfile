FROM clojure:lein-2.8.1-alpine as build

WORKDIR /app

ARG username
ARG password

ENV USERNAME=$username
ENV PASSWORD=$password

COPY ./project.clj .

RUN lein install

COPY ./resources ./resources
COPY ./src ./src

RUN lein uberjar


FROM openjdk:jre-alpine

COPY --from=build /app/target/beehive-database-*-standalone.jar /database.jar

ENTRYPOINT java -jar ./database.jar

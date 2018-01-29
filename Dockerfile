FROM clojure:lein-2.8.1-alpine as build

WORKDIR /app

ARG username
ARG password

ENV USERNAME=$username
ENV PASSWORD=$password

COPY ./project.clj /app
COPY ./src /app/src
COPY ./resources /app/resources

RUN lein ring uberjar

RUN mkdir /export && \
	cp /app/target/uberjar/beehive-database-*-standalone.jar /export/database.jar



FROM java:8-jre-alpine

COPY --from=build /export .

ENTRYPOINT java -jar database.jar

FROM clojure:lein-2.8.1-alpine

WORKDIR /app

COPY ./project.clj /app
COPY ./src /app/src
COPY ./test /app/test

ENTRYPOINT lein ring server

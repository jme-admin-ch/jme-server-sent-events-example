# JME Server-Sent Events Example

This repository demonstrates server-sent events (SSE) in jEAP applications. It contains two equivalent person
applications: one exposes its SSE stream anonymously, while the other protects the stream with OAuth2. Both applications
use PostgreSQL for persistence and Kafka to transport resource-mutation events to the SSE endpoint.

## Modules

| Module | Purpose |
| --- | --- |
| `jme-server-sent-events-auth-scs` | Local OAuth2 mock server |
| `jme-server-sent-events-scs-ui` | Angular frontend for the anonymous SSE example |
| `jme-server-sent-events-scs-web` | Person API and anonymous SSE endpoint |
| `jme-server-sent-events-secure-scs-ui` | Angular frontend for the authenticated SSE example |
| `jme-server-sent-events-secure-scs-web` | Person API and authenticated SSE endpoint |

## Prerequisites

- JDK 25
- Docker with Docker Compose v2
- Node.js 22.13 or newer and npm
- Google Chrome for the Playwright integration tests

Use the included Maven wrapper for all Maven commands.

## Build and test

```shell
./mvnw clean verify
```

The build runs both frontend unit-test suites, backend integration tests, and Playwright browser tests. The browser tests
start the Spring Boot applications with an embedded OAuth2 server and Kafka broker, exercise person CRUD through the UI,
and verify that a backend mutation reaches the browser through the SSE transport.

## Run locally

Start PostgreSQL, Kafka, and Schema Registry:

```shell
docker compose -f docker/docker-compose.yml up -d
```

Build and install all modules:

```shell
./mvnw install
```

Start the OAuth2 mock server and both applications in separate terminals:

```shell
./mvnw --projects jme-server-sent-events-auth-scs spring-boot:run \
  -Dspring-boot.run.profiles=local
```

```shell
./mvnw --projects jme-server-sent-events-scs/jme-server-sent-events-scs-web spring-boot:run \
  -Dspring-boot.run.profiles=local
```

```shell
./mvnw --projects jme-server-sent-events-secure-scs/jme-server-sent-events-secure-scs-web spring-boot:run \
  -Dspring-boot.run.profiles=local
```

Open the applications:

- Anonymous SSE: http://localhost:8080/jme-server-sent-events-scs/
- Authenticated SSE: http://localhost:8081/jme-server-sent-events-secure-scs/

Stop and remove the local infrastructure with:

```shell
docker compose -f docker/docker-compose.yml down -v
```

## Relevant implementation

The backend applications use `jeap-server-sent-events-starter`. Their `PersonService` publishes resource mutations and
their `application.yml` files select anonymous or OAuth2-protected SSE transport. The Angular frontends enable
`refreshOnPushEvent` for the persons table and subscribe to resource-mutation events through the Quadrel push-event
service.

## JME

This repository is part of the [JME open-source suite](https://github.com/jme-admin-ch/jme).

## License

This project is licensed under the [Apache License 2.0](LICENSE).

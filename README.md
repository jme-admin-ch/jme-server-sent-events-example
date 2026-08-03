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

The person API is available below each application at `/api/persons`; the SSE stream is available at
`/ui-api/sse/events`. API requests require a token in both applications. The anonymous example permits an unauthenticated
SSE connection, while the secure example requires the `jme_@sse_#read` role.

### Local services and credentials

All credentials below are local fixtures used only by this example.

| Service | Address | Local configuration |
| --- | --- | --- |
| OAuth2 mock server | http://localhost:8082/default-oauth-mock-server | Client `microService`, secret `secret` |
| Anonymous PostgreSQL | `localhost:5432/sample-db` | User `db-user`, password `db-pass`, schema `data` |
| Secure PostgreSQL | `localhost:5433/sample-secure-example-db` | User `db-user`, password `db-pass`, schema `data` |
| Kafka | `localhost:9092` | SASL/PLAIN user `user`, password `user-secret` |
| Schema Registry | http://localhost:7781 | No authentication |
| Monitoring endpoints | Below each application context path | User `prometheus`, password `secret` |

The OAuth mock's configured identity has ID `user`, preferred username `12345`, and the local person read/write and SSE
read roles. The browser clients use the same `microService` OAuth client as Swagger UI.

### Frontend development mode

For frontend hot reload, keep the infrastructure and OAuth mock server running. Start the anonymous backend with its
`local-ui` profile and the secure backend with both `local` and `local-ui` so the secure profile retains its database,
Kafka, OAuth, and port configuration:

```shell
./mvnw --projects jme-server-sent-events-scs/jme-server-sent-events-scs-web spring-boot:run \
  -Dspring-boot.run.profiles=local-ui
```

```shell
./mvnw --projects jme-server-sent-events-secure-scs/jme-server-sent-events-secure-scs-web spring-boot:run \
  -Dspring-boot.run.profiles=local,local-ui
```

Start both Angular development servers from the repository root:

```shell
npm --prefix jme-server-sent-events-scs/jme-server-sent-events-scs-ui start
```

```shell
npm --prefix jme-server-sent-events-secure-scs/jme-server-sent-events-secure-scs-ui run start:4201
```

Open the development frontends:

- Anonymous SSE: http://localhost:4200/
- Authenticated SSE: http://localhost:4201/

The Angular proxy configurations forward SSE requests to the anonymous backend on port `8080` and the secure backend on
port `8081`. API requests use the backend URLs supplied by each application's frontend configuration.

### IntelliJ IDEA

Shared run configurations are available under `.run/` for `OAuth-Mock-Server`, the anonymous `SCS (local)` and
`SCS (local-ui)`, and the corresponding `Secure SCS (local)` and `Secure SCS (local-ui)` backends. Start
`OAuth-Mock-Server` before either backend.

### Troubleshooting

- Start the OAuth mock before either person application. Otherwise issuer and JWK requests to port `8082` fail.
- Use `docker compose -f docker/docker-compose.yml ps` to inspect infrastructure startup. `broker_init` and `kafka-init`
  exiting with status `0` is expected after they configure the Kafka user and topics.
- The Compose setup creates `jme-server-sent-events-scs-notifyclient` and
  `jme-server-sent-events-secure-scs-notifyclient`. Check the initializer logs if an SSE event is not delivered.
- Ensure ports `4200`, `4201`, `5432`, `5433`, `7781`, `8080`, `8081`, `8082`, and `9092` are available.
- Reset stale local infrastructure with the `down -v` command below, then start Compose again.

Stop and remove the local infrastructure with:

```shell
docker compose -f docker/docker-compose.yml down -v
```

## Relevant implementation

### Kafka and SSE flow

The backend applications use `jeap-server-sent-events-starter`. When `PersonService` creates, updates or deletes a
person, it calls `ResourceMutationService.resourceMutation(...)`. The starter publishes a `NotifyClientCommand` to the
application's Kafka topic and every running backend instance consumes it. Each instance then forwards the mutation to
its connected browser clients through `/ui-api/sse/events`. This Kafka fan-out ensures that a browser receives the
notification even when its SSE connection terminates on a different backend instance from the one handling the change.

The examples use separate topics:

- `jme-server-sent-events-scs-notifyclient`
- `jme-server-sent-events-secure-scs-notifyclient`

Both applications declare producer and consumer contracts in their `Application` class and select their topic through
`jeap.sse.kafka.topic` in the corresponding defaults file. The local Docker Compose environment creates both topics and
provides Kafka on port `9092` and Schema Registry on port `7781`.

The Angular persons table enables `refreshOnPushEvent` with the resource identifier `persons`. On a resource-mutation
event, Quadrel reloads the current person data through the regular REST API rather than carrying the resource itself in
the SSE payload.

### Backend

- Starter dependency: the two web-module `pom.xml` files
- Message contracts: each web module's `ch.admin.bit.jme.Application`
- Resource mutations: each web module's `ch.admin.bit.jme.domain.PersonService`
- Topic and authorization configuration: `jme-server-sent-events-defaults.yml` and
  `jme-server-sent-events-secure-defaults.yml`
- Endpoint security: each web module's `ch.admin.bit.jme.web.config.WebSecurityConfig`

### Frontend

- `persons-overview.component.ts` enables `refreshOnPushEvent`, identifies the table as `persons`, and selects
  `RESOURCE_CREATED` and `RESOURCE_DELETED` through `refreshingEvents`.
- The same component provides `PersonTableDataResolverService` through `QD_TABLE_DATA_RESOLVER_TOKEN`, allowing Quadrel
  to reload the table through the regular REST API.
- `addSnackbarObservationToPushEventService` observes created, updated and deleted events and displays the corresponding
  user feedback.
- `pushevent.service.ts` connects the Quadrel push-event service to the backend SSE endpoint. The anonymous example uses
  `disableAuthentication: true`; the authenticated example sends its OAuth2 token.

For the reusable library behavior and configuration options, see the
[jEAP Server-Sent Events documentation](https://github.com/jeap-admin-ch/jeap-server-sent-events/tree/main/docs).

Platform deployment URLs and environment configuration intentionally live in the corresponding Nivel and RHOS wrapper
repositories; this repository contains only portable behavior and local development information.

## JME

This repository is part of the [JME open-source suite](https://github.com/jme-admin-ch/jme).

## License

This project is licensed under the [Apache License 2.0](LICENSE).

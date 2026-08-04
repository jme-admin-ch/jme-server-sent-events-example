# JME server sent events Example
This example shows how to use SSE (server sent events) in a jEAP microservice with Quadrel without a secured SSE endpoint.

## Running the example locally
* First you need Kafka and PostgresSQL running, both can be started using ```docker-compose -f docker/docker-compose.yml up```
* Execute mvn install on the root of the project to build the frontend and backends.
* Then you can start the services individually. Start jme-server-sent-events-auth-scs first.
* You can go to http://localhost:8080/jme-server-sent-events-scs to see the example in action.

## SSE relevant code/configuration

### Backend

- jeap-server-sent-events-starter dependency in jme-server-sent-events-scs/jme-server-sent-events-scs-web jme-server-sent-events-secure-scs/jme-server-sent-events-secure-scs-web pom.xml
- Resource mutation service usage in ch.admin.bit.jme.domain.PersonService (in both secure and not)
- jeap.sse.xy configuration in application.yml (in both secure and not)
- WebSecurityConfig (in both secure and not)

### Frontend

- refreshOnPushEvent: true, uid: 'persons'  in persons-overview.component.ts
- refreshingEvents: in persons-overview.component.ts
- providers: [ { provide: QD_TABLE_DATA_RESOLVER_TOKEN,  useClass: PersonTableDataResolverService  } ] in persons-overview.component.ts
- addSnackbarObservationToPushEventService in persons-overview.component.ts
- disableAuthentication in pushevent.service.ts

## Links to example

### Local

http://localhost:8080/jme-server-sent-events-scs

### AWS

#### DEV
https://jme-dev.ingress.nivel.bazg.admin.ch/jme-server-sent-events-scs/
https://jme-dev.ingress.nivel.bazg.admin.ch/jme-server-sent-events-secure-scs/

#### REF
https://jme-ref.ingress.nivel.bazg.admin.ch/jme-server-sent-events-scs/
https://jme-ref.ingress.nivel.bazg.admin.ch/jme-server-sent-events-secure-scs/

### RHOS
https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-server-sent-events-scs/
https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-server-sent-events-secure-scs/

## Documentation
https://confluence.bit.admin.ch/x/LFLFPw

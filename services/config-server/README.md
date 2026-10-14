# config-server

> Central place for the **non-secret** configuration of all TicketFlow services.

## Concept illustrated
Externalized configuration with Spring Cloud Config. Instead of copying the same
settings into every service, they live once in `config-repo/` and each service
fetches them at startup. A value can be changed and reloaded **without restarting**
the service (`/actuator/refresh`).

## Overview
- **Port:** 8888
- **Depends on:** nothing (it does not register in Eureka: services must find it *before* anything else)
- **Key URLs:** `/{application}/{profile}` (e.g. `/customer-service/default`), `/actuator/health`

The server resolves a request for application `customer-service` by merging, from the most to
the least specific:
1. `config-repo/customer-service.yml`
2. `config-repo/application.yml` (shared by everybody)

Values served by the server **win over the service's own `application.properties`**.
Settings needed to *reach* the infrastructure (Eureka URL, Config Server URL, ports) stay
local on purpose: a service cannot download them from a server it does not know yet.

> **Secrets never go in `config-repo/`.** Passwords and keys come from environment variables /
> a `.env` file (step-06) or a secrets manager. The Config Server serves plain files over HTTP.

## Run locally
    ./mvnw -pl services/config-server spring-boot:run
    curl http://localhost:8888/customer-service/default

## Run with Docker
    docker compose -f infra/docker-compose.yml up --build config-server

In Compose, `config-repo/` is **mounted** into the container, so editing a file is enough.

## Run the tests
    ./mvnw -pl services/config-server test

## Exercise: change a value without restarting
1. Start `config-server` then `customer-service`.
2. `curl localhost:8081/api/customers/config-demo` → message from `config-repo/customer-service.yml`.
3. With Compose (mounted folder) or `CONFIG_REPO=file:$PWD/config-repo/`, edit the message.
4. `curl -X POST localhost:8081/actuator/refresh` then call step 2 again: new value, no restart.
5. Stop the Config Server and restart `customer-service`: what happens, and why does it still start?

## Common pitfalls
- A file must be named after `spring.application.name` (`customer-service.yml`), otherwise it is never served.
- With the packaged default (`classpath:/config-repo/`) a change needs a rebuild: use `CONFIG_REPO=file:...`.
- `optional:` in `spring.config.import` makes a missing server silent: if a value looks wrong, check
  the service logs for the line saying where the configuration was fetched from.
- The refresh only affects `@RefreshScope` beans and `@ConfigurationProperties`, not everything.

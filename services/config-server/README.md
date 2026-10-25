# config-server

> Central place for the **non-secret** configuration of all TicketFlow services.

## Concept illustrated
Externalized configuration with Spring Cloud Config. Instead of copying the same
settings into every service, they live once, in a **Git repository**, and each service
fetches them at startup. A value can be changed and reloaded **without restarting**
the service (`/actuator/refresh`).

## Overview
- **Port:** 8888
- **Depends on:** nothing (it does not register in Eureka: services must find it *before* anything else)
- **Key URLs:** `/{application}/{profile}` (e.g. `/customer-service/default`), `/actuator/health`

The server resolves a request for application `customer-service` by merging, from the most to
the least specific:
1. `customer-service.yml`
2. `application.yml` (shared by everybody)

These files are the content of the `config-repo/` folder of this project: **it is what you put in the
Git repository** (see below).

Values served by the server **win over the service's own `application.properties`**.
Settings needed to *reach* the infrastructure (Eureka URL, Config Server URL, ports) stay
local on purpose: a service cannot download them from a server it does not know yet.

> **Secrets never go in the configuration repository.** A Git history is forever: a password committed once
> stays readable in every clone, even after you delete it. Passwords and keys come from environment variables /
> a `.env` file (step-06) or a secrets manager. The Config Server serves plain files over HTTP.

## Backends: where the configuration is read from

| Backend | When | How to select it |
|---|---|---|
| **Git** (default) | A shared configuration: every change is versioned, reviewed and can be rolled back | nothing: it is the default |
| **Local files** | A first run with no repository at all, tests | profile `native`, see the commented blocks in `application.yml` |
| Vault / encrypted values | Secrets | not used here: secrets stay in environment variables, see below |

### Using Git
1. Create a repository (a **dedicated** one is the cleanest: its own access rights and history) and
   put the content of `config-repo/` at its root: `application.yml`, `customer-service.yml`, ...
2. Give the server its address:

   | Variable | Meaning | Default |
   |---|---|---|
   | `CONFIG_GIT_URI` | repository URL, e.g. `https://github.com/<you>/ticketflow-config.git` | **required** |
   | `CONFIG_GIT_LABEL` | branch, tag or commit served by default | `main` |
   | `CONFIG_GIT_USERNAME` / `CONFIG_GIT_PASSWORD` | **private** repository only: user name and an access **token** | empty |
   | `CONFIG_GIT_REFRESH_RATE` | seconds between two fetches from the remote (`0` = every request) | `30` |

   If the files live in a sub-folder of a bigger repository (for example this one), add
   `SPRING_CLOUD_CONFIG_SERVER_GIT_SEARCH_PATHS=config-repo`.
3. Start it. The server **clones at startup**, so a wrong URL or wrong credentials fail immediately in
   the logs, not on the first request of the first service. Without `CONFIG_GIT_URI` it refuses to
   start and names the missing variable.

Use an access token with **read-only** rights on that single repository; the server never pushes.

### Using local files instead
Uncomment the `profiles` and `native` blocks of `application.yml` (they are kept there on purpose).
Packaged in the jar, `config-repo/` needs a rebuild to change; with
`CONFIG_REPO=file:$PWD/config-repo/` a change is served immediately. In Docker Compose see the
commented lines of the `config-server` service.

## Run locally
    export CONFIG_GIT_URI=https://github.com/<you>/ticketflow-config.git
    ./mvnw -pl services/config-server spring-boot:run
    curl http://localhost:8888/customer-service/default

## Run with Docker
    # CONFIG_GIT_URI (and, for a private repository, the token) in infra/.env or exported
    docker compose -f infra/docker-compose.yml up --build config-server

## Run the tests
    ./mvnw -pl services/config-server test

`ConfigServerGitBackendTest` creates a real Git repository in a temporary folder, so it needs no
network and no account. `ConfigServerApplicationTests` covers the local-files backend.

## Exercise: change a value without restarting
1. Start `config-server` then `customer-service`.
2. `curl localhost:8081/api/customers/config-demo` -> message from `customer-service.yml`.
3. Edit the message in the Git repository, commit and **push**.
4. Wait for `CONFIG_GIT_REFRESH_RATE` seconds (or set it to `0`), then
   `curl -X POST localhost:8081/actuator/refresh` and call step 2 again: new value, no restart.
5. Stop the Config Server and restart `customer-service`: what happens, and why does it still start?
6. Push a deliberately broken YAML file. What does the Config Server answer, and what does the service do?

## Common pitfalls
- A file must be named after `spring.application.name` (`customer-service.yml`), otherwise it is never served.
- A push is not instantly visible: the server fetches at most every `CONFIG_GIT_REFRESH_RATE` seconds, and a
  client only reloads when `POST /actuator/refresh` is called. Nothing pushes the change to the services yet.
- The server serves the label `main` by default. Changes on another branch are invisible until you ask for
  it (`/customer-service/default/my-branch`) or change `CONFIG_GIT_LABEL`.
- With the local-files backend and the packaged default (`classpath:/config-repo/`) a change needs a
  rebuild: use `CONFIG_REPO=file:...`.
- `optional:` in `spring.config.import` makes a missing server silent: if a value looks wrong, check
  the service logs for the line saying where the configuration was fetched from.
- The refresh only affects `@RefreshScope` beans and `@ConfigurationProperties`, not everything.

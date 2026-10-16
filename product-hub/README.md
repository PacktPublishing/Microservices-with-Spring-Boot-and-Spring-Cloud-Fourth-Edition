# product-hub

Modern rebuild of the book's product microservices: Spring Boot 4.1 + Spring MVC, Java 25, Gradle Kotlin DSL, one Postgres with a schema per core service.

| Module | Port |
|--------|------|
| `product-composite-service` | 7000 |
| `product-service` | 7001 |
| `recommendation-service` | 7002 |
| `review-service` | 7003 |

## Prerequisites

- JDK 17+ to run Gradle. The build uses a Java 25 toolchain; Gradle downloads it if it is not installed.
- Docker with Compose v2.
- Spring Boot CLI 4.1.1 — only needed to regenerate modules.

## Build

```powershell
.\gradlew.bat build
```

## Run a service

```powershell
.\gradlew.bat :product-service:bootRun
curl http://localhost:7001/actuator/health
```

Swap the module name for the other services.

## Postgres

```powershell
Copy-Item .env.example .env   # first time only; edit the password
docker compose up -d
docker compose exec postgres psql -U producthub -d producthub -c '\dn'
docker compose down           # add -v to wipe data
```

Init scripts in `docker/postgres/init/` run only when the volume is empty.

## Regenerate modules

`create-projects.ps1` holds the `spring init` commands used to create the modules. Generated output was then merged into this multi-project build (single wrapper, root `settings.gradle.kts`, `application.yml`); see `../context/features/phase-0-scaffold.md`.

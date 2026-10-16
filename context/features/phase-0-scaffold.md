# Phase 0 — Scaffold `product-hub`

Create the empty-but-runnable skeleton of the modern project: four Spring Boot service modules generated with **Spring Boot CLI** (`spring init`), wired into one Gradle multi-project build, plus a Docker Compose Postgres with one schema per core service.

No business endpoints, no persistence code, no Feign clients yet — those start in Phase 1 / Phase 2.

## Decisions (locked for this phase)

| Topic | Choice |
|-------|--------|
| Generator | Spring Boot CLI `spring init` 4.1.1 (agent installs from the official Maven Central zip) |
| Spring Boot | **4.1.1** (Spring Framework 7, Spring Cloud 2025.1 BOM added by Initializr for OpenFeign) |
| Java | **25 LTS** via Gradle toolchain |
| Build | Gradle, **Kotlin DSL** (`--type=gradle-project-kotlin`) |
| groupId | `com.minhhung.producthub` |
| Version | `0.0.1-SNAPSHOT` |
| Postgres | `postgres:18` in Compose; one database `producthub`, schemas `product`, `recommendation`, `review` |

### Services

| Module | Package | Port | Initializr dependencies |
|--------|---------|------|-------------------------|
| `product-service` | `com.minhhung.producthub.product` | 7001 | `web,actuator,validation,springdoc-openapi` |
| `recommendation-service` | `com.minhhung.producthub.recommendation` | 7002 | `web,actuator,validation,springdoc-openapi` |
| `review-service` | `com.minhhung.producthub.review` | 7003 | `web,actuator,validation,springdoc-openapi` |
| `product-composite-service` | `com.minhhung.producthub.composite` | 7000 | `web,actuator,validation,springdoc-openapi,cloud-feign` |

Why this dependency set:

- `web` = Spring MVC (locked; no `webflux`).
- `validation` and `springdoc-openapi` are needed in Phase 1; adding them through Initializr now avoids hand-editing versions later.
- `cloud-feign` on the composite only, so Initializr wires the Spring Cloud BOM correctly. `@EnableFeignClients` is **not** added until Phase 1.
- **Not** added yet: `data-jpa`, `flyway`, `postgresql`. They would force a datasource at startup and break the "boots without a DB" goal. They arrive in Phase 2.
- **Not** added: `docker-compose` (Spring Boot Docker Compose support) — it auto-starts containers on `bootRun`, which hides what is running. Compose is started explicitly.

## Target layout

```text
product-hub/
  create-projects.ps1          # reproducible spring init commands (like the book's create-projects.bash)
  settings.gradle.kts          # rootProject + include(...) + foojay toolchain resolver
  gradlew, gradlew.bat, gradle/wrapper/   # single wrapper at the root
  .gitignore
  .env.example                 # committed; .env is not
  docker-compose.yml           # postgres only in Phase 0
  docker/postgres/init/01-create-schemas.sql
  README.md
  product-service/
    build.gradle.kts
    src/main/java/com/minhhung/producthub/product/ProductServiceApplication.java
    src/main/resources/application.yml
    src/test/java/.../ProductServiceApplicationTests.java
  recommendation-service/      # same shape
  review-service/              # same shape
  product-composite-service/   # same shape, package …composite
```

## Install Spring Boot CLI

Windows has no SDKMAN, so install the official binary zip:

1. Download `https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-cli/4.1.1/spring-boot-cli-4.1.1-bin.zip`.
2. Extract to `%USERPROFILE%\tools\spring-4.1.1`.
3. Add `%USERPROFILE%\tools\spring-4.1.1\bin` to the user `PATH`.
4. Verify with `spring --version`.

## Generation commands

`product-hub/create-projects.ps1`, run from `product-hub/` (PowerShell line continuation is a backtick):

```powershell
$common = @(
  '--boot-version=4.1.1',
  '--type=gradle-project-kotlin',
  '--java-version=25',
  '--packaging=jar',
  '--group-id=com.minhhung.producthub',
  '--version=0.0.1-SNAPSHOT'
)
$coreDeps = 'web,actuator,validation,springdoc-openapi'

spring init @common --name=product-service --artifact-id=product-service `
  --package-name=com.minhhung.producthub.product --dependencies=$coreDeps product-service

spring init @common --name=recommendation-service --artifact-id=recommendation-service `
  --package-name=com.minhhung.producthub.recommendation --dependencies=$coreDeps recommendation-service

spring init @common --name=review-service --artifact-id=review-service `
  --package-name=com.minhhung.producthub.review --dependencies=$coreDeps review-service

spring init @common --name=product-composite-service --artifact-id=product-composite-service `
  --package-name=com.minhhung.producthub.composite --dependencies="$coreDeps,cloud-feign" product-composite-service
```

Check CLI and options first: `spring --version`, `spring init --list`.

## Post-generation steps

`spring init` creates four standalone builds. Turn them into one multi-project build:

1. Move one module's `gradlew`, `gradlew.bat`, and `gradle/wrapper/` to `product-hub/`; delete the wrapper files and `settings.gradle.kts` from all four modules. Delete generated `HELP.md` files and the empty `src/main/resources/static/` and `templates/` folders (JSON APIs only, no server-side views).
2. Create `product-hub/settings.gradle.kts`:
   - `rootProject.name = "product-hub"`
   - `include("product-service", "recommendation-service", "review-service", "product-composite-service")`
   - `org.gradle.toolchains.foojay-resolver-convention` plugin, so Gradle can download JDK 25 when the machine only has JDK 21.
3. Keep each module's `build.gradle.kts` as generated (plugin versions per module, like the book). Do not add a root `build.gradle.kts` or shared convention plugin in this phase.
4. Replace each `application.properties` with `application.yml`:
   - `spring.application.name`
   - `server.port` from the table above
   - `management.endpoints.web.exposure.include: health,info`
5. Merge per-module `.gitignore` files into `product-hub/.gitignore` (`build/`, `.gradle/`, IDE files, `.env`).
6. Keep the generated `contextLoads` test in each module.

## Docker Compose (Postgres only)

- Service `postgres`, image `postgres:18`, port `5432:5432`.
- `POSTGRES_DB=producthub`; `POSTGRES_USER` / `POSTGRES_PASSWORD` from `.env` (values in `.env.example` are placeholders for local use only).
- Named volume mounted at `/var/lib/postgresql` (Postgres 18 image layout).
- `./docker/postgres/init` mounted at `/docker-entrypoint-initdb.d`; `01-create-schemas.sql` creates `product`, `recommendation`, `review` with `CREATE SCHEMA IF NOT EXISTS`.
- Healthcheck with `pg_isready`.
- One shared DB user in Phase 0. Per-service roles with schema-scoped grants are a Phase 2 decision.

## Acceptance criteria

- [x] `product-hub/create-projects.ps1` regenerates the four modules with the commands above.
- [x] From `product-hub/`: `.\gradlew.bat build` succeeds for all four modules (tests included) on a machine with only JDK 21 installed (toolchain provisions 25).
- [x] `.\gradlew.bat :product-service:bootRun` (and the other three) start on their ports; `GET /actuator/health` returns `UP` without Postgres running.
- [x] `.\gradlew.bat :product-service:dependencies` shows `spring-webmvc` and no `spring-webflux` (all four modules).
- [x] `docker compose up -d` → `postgres` is healthy; `\dn` inside the container lists `product`, `recommendation`, `review`.
- [x] Packages match `com.minhhung.producthub.<service>`; no shared `common` / `api` module.
- [x] No `.env`, `build/`, or `.gradle/` committed.
- [x] `product-hub/README.md` documents: prerequisites, regenerate, build, run each service, start/stop Postgres.

## Out of scope (later phases)

- REST endpoints, DTOs, Feign clients, `@EnableFeignClients` — Phase 1
- Service Dockerfiles and services in Compose — Phase 1
- JPA, Flyway, Postgres driver, Testcontainers — Phase 2
- Security, resilience, tracing — Phases 3–4
- Root convention plugin / version catalog — only if duplication across the four `build.gradle.kts` files becomes painful

## Implementation notes

- Work on branch `feature/phase-0-scaffold`.
- If Initializr rejects a dependency id or version range at generation time, stop and record it here rather than switching stacks.
- Spring Boot CLI 4.x uses `--group-id` / `--artifact-id` (the book's 3.x scripts use `--groupId`).
- With Boot 4, Initializr's `web` id generates `spring-boot-starter-webmvc`, plus per-starter test starters (`*-test`).
- Generated versions: Gradle 9.7.1, Spring Cloud 2025.1.3, springdoc 3.1.0, foojay resolver 1.0.0.
- First toolchain download on Windows can fail to move the extracted JDK into `~/.gradle/jdks`; rerunning the build succeeded.

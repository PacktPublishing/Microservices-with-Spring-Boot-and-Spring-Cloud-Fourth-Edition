# Project overview

**Product Hub** (`product-hub/`) — modern microservices playground; learning rewrite of *Microservices with Spring Boot and Spring Cloud* (Packt, 4th edition / Magnus Larsson).

---

## Problem / purpose

The book samples teach production ideas well, but the stack choices (WebFlux, Netflix Eureka, mixed Mongo/MySQL, Spring Cloud Stream + Kafka/Rabbit, Spring Cloud Gateway) are not always what you want for day-to-day senior Java practice or a clean portfolio.

This project keeps the **book chapter folders as references** and builds a **separate modern system** that:

- Teaches the same distributed-system concerns end-to-end
- Uses interview- and industry-friendly defaults where we deliberately diverge
- Grows in phases so each concern is learnable in isolation

---

## Audience

| Persona | Need |
|---------|------|
| Owner (learner) | Senior Java prep: design, Spring Boot, Docker/K8s concepts, clear ADRs |
| AI coding agents | Locked stack + phased scope; no surprise tech from deferred list |
| Future reviewers | Readable architecture and runbook in the modern root |

---

## Goals

- Learn book concepts: cooperating services → Docker → security → resilience → observability → K8s ideas.
- Rebuild with clearer structure and locked modern choices (see Tech stack).
- Prefer incremental vertical slices over copying the book stack verbatim.

**Non-goals (early):** perfect prod parity, full mesh/logging suite, native images, multi-broker messaging.

---

## Repository relationship

| Area | Role |
|------|------|
| `Chapter03/` … `Chapter23/` | Book snapshots — read/run to learn; do not mass-refactor |
| `product-hub/` | Modern implementation root (Phase 0+) |
| `context/` | Specs, standards, current work, feature/fix/research docs |

Default modern-feature work to `product-hub/` once it exists, unless a book chapter is named.

---

## Tech stack (locked)

| Category | Choice |
|----------|--------|
| Runtime / framework | **Spring Boot 4.1.x** (Spring Framework 7) |
| Web | **Spring MVC** (`starter-web`) — **not** WebFlux |
| Language | **Java 25 LTS** (Gradle toolchain) |
| Base package | **`com.minhhung.producthub.*`** |
| Build | Gradle multi-module, **Kotlin DSL** (service modules only) |
| Scaffolding | Spring Boot CLI `spring init` |
| Spring Cloud | 2025.1.x BOM (OpenFeign) |
| HTTP between services | **OpenFeign** |
| Public entry (early) | **Composite as BFF** (no gateway required locally) |
| API gateway (later) | **Kong** — optional locally; important for prod-like edge |
| Database | **One PostgreSQL**, **multiple schemas** (per service) |
| Migrations | Flyway (preferred) |
| Messaging (later) | **Kafka + Avro** via **`spring-kafka`** only |
| Messaging (out) | **No RabbitMQ**; **no** Spring Cloud Stream / Cloud binders |
| Security | Spring Security (JWT resource server first; external IdP later) |
| Discovery (local) | Docker Compose DNS / hostnames |
| Discovery (K8s) | Kubernetes Services |
| Config (early) | `application.yml` + profiles + env |
| API docs | OpenAPI / springdoc + Swagger UI |
| Observability (phased) | Actuator, Micrometer, tracing (e.g. Zipkin/Tempo) |

### Clarification: Spring Boot vs Spring MVC

**Spring Boot** is the application framework. **Spring MVC** is the servlet/blocking web stack inside Boot. We use Boot + MVC, not WebFlux.

---

## Core services

| Service | Responsibility | Postgres schema (planned) | Base package (planned) |
|---------|----------------|---------------------------|------------------------|
| `product-service` | Product resource | `product` | `com.minhhung.producthub.product` |
| `recommendation-service` | Recommendations for a product | `recommendation` | `com.minhhung.producthub.recommendation` |
| `review-service` | Reviews for a product | `review` | `com.minhhung.producthub.review` |
| `product-composite-service` | BFF / aggregator (Feign → core services) | optional / none early | `com.minhhung.producthub.composite` |

**Modules:** service modules only — **no** shared `libs/common` or `libs/api`. DTOs, Feign clients, and helpers live inside each service.

### Package layout (Architecture Rules)

Each service follows a DDD-style layered package structure:

```text
…/<service>/src/main/java/com/minhhung/producthub/<boundedcontext>/
  controller/
  service/
  repository/
  entity/
  dto/
```

Example: `com.minhhung.producthub.product.controller`, `…product.service`, `…product.entity`, etc.

Optional packages when needed (still inside the same service): `config/`, `client/` (Feign), `exception/`, `mapper/`. Do not invent a shared common module for these.

---

## Architecture (early phases)

```text
Client
  → product-composite-service (BFF, Feign)
       → product-service
       → recommendation-service
       → review-service
  → PostgreSQL (one instance, multiple schemas)
```

Later: Kong in front of composite (prod-like edge); Kafka + Avro for async; Kubernetes.

**Local gateway:** not required to develop. Call the BFF directly until Phase 5. Kong is for learning edge concerns and prod-like routing—not a day-one dependency.

### Monorepo shape (`product-hub/`)

```text
product-hub/
  product-service/
  recommendation-service/
  review-service/
  product-composite-service/
  docker-compose.yml
  settings.gradle   # includes only the service modules
  README.md
```

---

## Locked decisions (summary)

| Topic | Decision |
|-------|----------|
| Web | Spring Boot + MVC (no WebFlux) |
| DB | One Postgres, multiple schemas |
| Sync RPC | OpenFeign |
| Edge early | Composite BFF; Kong later |
| Events | Kafka + Avro + `spring-kafka` only; no RabbitMQ; no Spring Cloud Stream |
| Modules | **Service modules only** — no `common` / shared `api` library |
| Project root | **`product-hub/`** |
| Base package | **`com.minhhung.producthub.<service>`** |
| Package layout | DDD-style: `controller`, `service`, `repository`, `entity`, `dto` |
| Entity equality | `equals` / `hashCode` on **business keys** (not DB surrogate id alone when unstable) |
| Netflix OSS | No Eureka (etc.) |
| Config early | Files + env (not Config Server by default) |

---

## Phased roadmap

| Phase | Focus | Success sketch |
|-------|--------|----------------|
| **0** Setup | `product-hub/`, Gradle service modules, Compose + Postgres | Skeleton builds; DB up |
| **1** Cooperating services | 4× MVC apps, Feign aggregate, OpenAPI, Compose | Composite GET by product id works |
| **2** Persistence | JPA/JDBC + Flyway per schema, Testcontainers | Data persists; tests green |
| **3** Security | Spring Security JWT on composite (+ harden cores) | No token → 401; valid JWT → OK |
| **4** Resilience & observability | Resilience4j on Feign; Actuator/tracing | Breaker + cross-service traces demable |
| **5** Kong | Edge in front of BFF | External traffic via Kong |
| **5b** Kafka + Avro | `spring-kafka` + schema registry | One produce/consume path E2E |
| **6** Config & discovery hygiene | Compose/K8s DNS; light config | No Eureka/Config Server required |
| **7** Kubernetes | Helm (or equiv.), selective Ch.15–17 | Happy path on local cluster |
| **8** Polish | ADRs, runbook, diagram | Portfolio-ready README |

Detail for each phase can live under `context/features/` when implementation starts.

---

## Book chapter → modern mapping

| Book | Learn from | Modern action |
|------|------------|---------------|
| Ch.3–5 | Split, REST, OpenAPI, Docker | Phase 1 |
| Ch.6 | Persistence, Testcontainers | Phase 2 (Postgres only) |
| Ch.7 | Messaging motivation | Phase 5b Kafka+Avro; no WebFlux |
| Ch.8–9 | Cloud / Eureka | Skip Netflix; Compose/K8s DNS |
| Ch.10 | Gateway | Phase 5 Kong (after BFF) |
| Ch.11 | OAuth/OIDC | Phase 3 Spring Security |
| Ch.12 | Central config | Phase 6 light approach |
| Ch.13–14 | Resilience, tracing | Phase 4 |
| Ch.15–17 | K8s, Helm, ConfigMap/Ingress | Phase 7 |
| Ch.18–20 | Istio, EFK, full Grafana | Deferred / selective |
| Ch.23 | GraalVM native | Deferred |

---

## Deferred / out of scope

**Deferred in timing** (do not start early unless asked):

- Kong before BFF is stable
- Kafka + Avro until Phase 5b
- External IdP (Keycloak/Auth0) before local JWT works
- Full K8s polish, rich Grafana alerting
- CQRS, transactional outbox, sagas, multi-tenant

**Out of scope for the modern project:**

- RabbitMQ
- Spring Cloud Stream / Cloud Kafka or Rabbit binders
- WebFlux / reactive drivers as default
- Netflix Eureka and Spring Cloud Gateway (book path)
- MongoDB / MySQL multi-store setup
- Spring Cloud Config Server as default
- Istio, full EFK, GraalVM (unless explicitly pulled from deferred later)
- Separate Postgres instance per service (multi-schema is the early choice)
- Shared `libs/common` or `libs/api` modules (duplicate DTOs/clients per service instead)

---

## Status

| Item | State |
|------|--------|
| Context docs | Active |
| Modern root folder | **`product-hub/`** (not created yet — Phase 0) |
| Base package | `com.minhhung.producthub.*` |
| Current implementation focus | See [`current-feature.md`](current-feature.md) |

---

## Decision log

| Date | Decision |
|------|----------|
| 2026-09-10 | Postgres: one instance, multiple schemas |
| 2026-09-10 | Inter-service calls: OpenFeign |
| 2026-09-10 | Edge: composite-as-BFF first; Kong later |
| 2026-09-10 | Web: Spring Boot + Spring MVC (not WebFlux) |
| 2026-09-10 | Context living docs under `context/` |
| 2026-09-11 | Messaging: Kafka + Avro only; no RabbitMQ; no Spring Cloud Stream — use `spring-kafka` |
| 2026-09-11 | Rename/align docs: `project-overview.md` + context README pattern |
| 2026-09-11 | No shared common/api modules — service modules only; DDD packages; entity equals/hashCode via business keys |
| 2026-09-11 | Project name: `product-hub`; base package: `com.minhhung.producthub.*` |
| 2026-09-27 | Scaffold with Spring Boot CLI; Spring Boot 4.1.1 (Initializr no longer offers 3.5.x); Java 25 LTS; Gradle Kotlin DSL |

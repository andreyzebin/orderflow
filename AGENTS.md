# Agent Guide — FlowMart Order Service

## What this service does

`order-service` is the FlowMart B2B order-management backend. Deployed as a
horizontally scaled Spring Boot application — concurrency matters.

## Engineering principles

- **SOLID** — keep service classes single-purpose; depend on abstractions at
  module boundaries; favour composition.
- **Constructor injection only** — no field or setter injection on services
  or controllers. Final fields where the injector allows.
- **No N+1 queries** — never call a repository inside a loop over a
  collection. Use batched / `In`-style queries.
- **Atomic writes** — methods that perform two or more writes must be
  transactional. The annotation lives on the service layer.
- **Authorisation** — every endpoint reading or mutating user-owned data
  must verify the caller owns the resource. Throw the framework's access-
  denied exception on failure; never 404 (it leaks existence).

## Tests

- New code ships with unit tests covering happy path + at least one error
  path. Use the existing fixtures under `src/test/java`.
- Bug fixes ship with a regression test pinned to the symptom.

## Style

- Javadoc on every public method of services and controllers.
- No commented-out code; no naked `// TODO` without an issue link.

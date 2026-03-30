# FlowMart — Order Service

Order management backend for the FlowMart B2B e-commerce platform.
Handles the full lifecycle of a retail order: creation, pricing, discounts,
fulfilment, and cancellation.

## Tech stack

- Java 17, Spring Boot 3.2
- Spring Data JPA / Hibernate, PostgreSQL
- Spring Security (JWT)
- Lombok, MapStruct
- JUnit 5, Mockito

## Local setup

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Requires a running PostgreSQL instance. See `src/main/resources/application-local.yml`.

## Key packages

| Package | Responsibility |
|---------|---------------|
| `model` | JPA entities |
| `repository` | Spring Data repositories |
| `service` | Business logic — all writes go through here |
| `controller` | REST layer — validation and auth only, no business logic |
| `exception` | Exception types and global handler |

## Running tests

```bash
./gradlew test
```

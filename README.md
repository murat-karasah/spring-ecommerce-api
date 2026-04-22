# Spring Boot E-Commerce REST API

Production-ready RESTful API built with Spring Boot 3.5 and Java 21.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.5, Spring Security 6 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate 6 |
| Auth | JWT (JJWT 0.12) — access + refresh tokens |
| Mapping | MapStruct 1.5 |
| Docs | Springdoc OpenAPI 3 (Swagger UI) |
| Infra | Docker Compose |
| Tests | JUnit 5, Mockito |

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21+
- Maven (wrapper included)

### Run with Docker Compose

```bash
docker compose up -d
```

App starts on **http://localhost:8080**  
Swagger UI: **http://localhost:8080/swagger-ui/index.html**  
Adminer (DB UI): **http://localhost:8090**

### Run locally (without Docker)

```bash
# Start only PostgreSQL
docker compose up -d postgres

# Run the app
./mvnw spring-boot:run
```

### Environment variables

| Variable | Default (dev) | Description |
|----------|--------------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/ecommerce_db` | JDBC URL |
| `DB_USERNAME` | `ecommerce_user` | DB user |
| `DB_PASSWORD` | `ecommerce_pass` | DB password |
| `JWT_SECRET` | `changeme-...` | HS512 signing key (min 32 chars) |
| `JWT_ACCESS_EXPIRATION` | `900000` | Access token TTL (ms) |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Refresh token TTL (ms) |

## API Overview

### Authentication — `/api/v1/auth`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/register` | Register new user → returns tokens |
| POST | `/login` | Login → returns tokens |
| POST | `/refresh` | Exchange refresh token for new access token |

### Products — `/api/v1/products`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | Public | List with filtering & pagination |
| GET | `/{id}` | Public | Get by id |
| POST | `/` | ADMIN | Create |
| PUT | `/{id}` | ADMIN | Update |
| DELETE | `/{id}` | ADMIN | Delete |

Query params: `page`, `size`, `sort`, `category`, `minPrice`, `maxPrice`, `search`

### Categories — `/api/v1/categories`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | Public | List all |
| GET | `/{id}` | Public | Get by id |
| POST | `/` | ADMIN | Create |
| PUT | `/{id}` | ADMIN | Update |
| DELETE | `/{id}` | ADMIN | Delete |

### Cart — `/api/v1/cart`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | USER | Get current cart |
| POST | `/items` | USER | Add item (auto-increments) |
| PUT | `/items/{itemId}` | USER | Update quantity |
| DELETE | `/items/{itemId}` | USER | Remove item |
| DELETE | `/` | USER | Clear cart |

### Orders — `/api/v1/orders`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/` | USER | Place order from cart |
| GET | `/my` | USER | My orders (paginated) |
| GET | `/my/{id}` | USER | My order by id |
| GET | `/` | ADMIN | All orders |
| GET | `/{id}` | ADMIN | Any order by id |
| PATCH | `/{id}/status` | ADMIN | Update status |

Order statuses: `PENDING` → `PAID` → `SHIPPED` → `DELIVERED` / `CANCELLED`

## Seed Data

Two users are seeded via `V2__seed_data.sql`:

| Email | Password | Role |
|-------|----------|------|
| `admin@example.com` | `Admin1234!` | ADMIN |
| `user@example.com` | `User1234!` | USER |

## Running Tests

```bash
./mvnw test
```

Unit tests cover `ProductService`, `AuthService`, and `OrderService` with Mockito mocks (20 tests, no DB required).

## Project Structure

```
src/main/java/com/example/ecommerce/
├── config/          # Security, JWT filter, OpenAPI, MapperConfig
├── controller/      # REST controllers
├── dto/             # Request / response records
├── entity/          # JPA entities
├── enums/           # OrderStatus, RoleName
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── helper/          # CurrentUserHelper
├── mapper/          # MapStruct interfaces
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic
└── specification/   # ProductSpecification (dynamic filtering)
```

<p align="center">
  <h1 align="center">🛒 E-Commerce Platform API</h1>
  <p align="center">
    A production-grade, enterprise-ready REST API for e-commerce operations.<br/>
    Built with <strong>Spring Boot 3.5</strong> · <strong>Java 21</strong> · <strong>PostgreSQL</strong> · <strong>Stripe</strong>
  </p>
  <p align="center">
    <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" alt="Java 21"/>
    <img src="https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=flat-square&logo=springboot" alt="Spring Boot 3.5"/>
    <img src="https://img.shields.io/badge/PostgreSQL-18-blue?style=flat-square&logo=postgresql" alt="PostgreSQL"/>
    <img src="https://img.shields.io/badge/Stripe-Integrated-blueviolet?style=flat-square&logo=stripe" alt="Stripe"/>
    <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker" alt="Docker"/>
    <img src="https://img.shields.io/badge/License-Proprietary-red?style=flat-square" alt="License"/>
  </p>
</p>

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Documentation](#api-documentation)
- [Database Migrations](#database-migrations)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

This repository contains the backend API for a full-featured e-commerce platform. It handles the complete purchase lifecycle—from user registration and product browsing, through cart management and Stripe-powered checkout, to order fulfillment and admin dashboards.

The system is designed with **security**, **reliability**, and **observability** as first-class concerns, making it suitable for production workloads.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                               │
│                  (Web / Mobile / Third-Party)                        │
└─────────────────────────────┬────────────────────────────────────────┘
                              │  HTTPS
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│                       FILTER CHAIN (ordered)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  ┌────────────┐  │
│  │ Correlation  │→ │  Rate Limit  │→ │ JWT Auth  │→ │   Audit    │  │
│  │  ID Filter   │  │   Filter     │  │  Filter   │  │  Logging   │  │
│  └──────────────┘  └──────────────┘  └───────────┘  └────────────┘  │
└─────────────────────────────┬────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER (REST)                         │
│  Auth · Product · Category · Cart · Checkout · Order · Review        │
│  Wishlist · Coupon · PaymentMethod · User · Admin · Webhook          │
└─────────────────────────────┬────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER (Business Logic)                 │
│  AuthService · ProductService · CartService · CheckoutService        │
│  OrderService · CouponService · ImageUploadService · AdminService    │
│  StripeWebhookService · PaymentValidationScheduler                   │
└──────────┬──────────────────┬──────────────────┬─────────────────────┘
           │                  │                  │
           ▼                  ▼                  ▼
┌────────────────┐  ┌─────────────────┐  ┌───────────────────┐
│  PostgreSQL    │  │   Stripe API    │  │    Cloudinary     │
│  (JPA / Flyway)│  │  (Payments)     │  │  (Image CDN)      │
└────────────────┘  └─────────────────┘  └───────────────────┘
```

### Design Principles

| Principle | Implementation |
|-----------|----------------|
| **Layered Architecture** | Controller → Service → Repository separation with strict dependency direction |
| **Stateless Auth** | JWT-based authentication; no server-side sessions |
| **Idempotent Payments** | Client-generated idempotency keys prevent duplicate Stripe charges |
| **Optimistic Locking** | `@Version` on all entities prevents lost-update anomalies |
| **Resilient Integrations** | `@Retryable` with exponential backoff for external API calls |
| **Schema-First DB** | Flyway migrations ensure reproducible, version-controlled schema evolution |
| **Structured Error Handling** | Global `@RestControllerAdvice` with typed `ErrorCode` enum for consistent error responses |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Runtime** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.5, Spring Security, Spring Data JPA, Spring Retry |
| **Database** | PostgreSQL 17+ with Hibernate 6 ORM |
| **Migrations** | Flyway |
| **Authentication** | JWT (JJWT 0.12) with BCrypt password hashing |
| **Payments** | Stripe Java SDK 29.x — Checkout Sessions, Webhooks |
| **Media Storage** | Cloudinary SDK — image upload, transformation, CDN delivery |
| **API Docs** | SpringDoc OpenAPI 3.0 with Swagger UI (custom dark theme) |
| **Observability** | Spring Boot Actuator — health, info, metrics, Prometheus endpoint |
| **Build** | Apache Maven |
| **Containerization** | Docker (multi-stage build with Eclipse Temurin 21) |

---

## Project Structure

```
src/main/java/com/ecommerce/shop/
├── config/                  # Security, CORS, OpenAPI, Cloudinary, Filter configs
│   ├── SecurityConfig       # Filter chain, RBAC, CORS, auth providers
│   ├── AuditLoggingFilter   # Request/response audit trail
│   ├── CorrelationIdFilter  # X-Request-Id propagation for tracing
│   ├── RateLimitFilter      # Per-IP request throttling
│   ├── CloudinaryConfig     # Image CDN bean configuration
│   └── OpenApiConfig        # Swagger grouping (public, shopping, all)
│
├── controller/              # REST endpoints (13 controllers)
│   ├── AuthController       # Register, login
│   ├── ProductController    # CRUD + search + image upload
│   ├── CategoryController   # CRUD with nested products
│   ├── CartController       # Add, update, remove, clear
│   ├── CheckoutController   # Create Stripe checkout session
│   ├── OrderController      # User orders, admin status updates
│   ├── CouponController     # Admin CRUD + user validation
│   ├── ReviewController     # Product reviews & ratings
│   ├── WishlistController   # User wishlists
│   ├── UserController       # Profile, addresses, password
│   ├── PaymentMethodController
│   ├── AdminController      # Dashboard analytics
│   └── StripeWebhookController
│
├── dto/
│   ├── request/             # Validated inbound payloads (14 DTOs)
│   └── response/            # Outbound payloads with ApiResponse<T> envelope
│
├── entity/                  # JPA entities with BaseEntity (audit + versioning)
│   ├── BaseEntity           # id, version, createdAt, updatedAt
│   ├── User, Product, Category, Cart, CartItem, Order, OrderItem
│   ├── CheckoutSession, CheckoutSessionItem, WebhookEvent
│   ├── Coupon, Review, Wishlist, Address
│
├── enums/                   # Type-safe enumerations
│   ├── Role                 # ADMIN, CUSTOMER
│   ├── OrderStatus          # PENDING → SHIPPED → DELIVERED → CANCELLED
│   ├── PaymentStatus        # PENDING, PAID, FAILED, EXPIRED
│   ├── ProductStatus        # ACTIVE, INACTIVE
│   ├── DiscountType         # PERCENTAGE, FIXED
│   └── ErrorCode            # Typed error classification
│
├── exception/               # Centralized error handling
│   ├── GlobalExceptionHandler   # @RestControllerAdvice
│   ├── BusinessException        # Domain rule violations
│   └── ResourceNotFoundException
│
├── repository/              # Spring Data JPA repositories (14)
│
├── security/                # JWT infrastructure
│   ├── JwtService           # Token generation & validation
│   ├── JwtAuthFilter        # Request authentication filter
│   └── UserDetailsServiceImpl
│
└── service/                 # Business logic (14 services)
    ├── CheckoutService          # Cart → Stripe session → DB snapshot
    ├── CheckoutPaymentProcessor # Idempotent order creation from paid sessions
    ├── StripeWebhookService     # Webhook event processing
    ├── PaymentValidationScheduler # Periodic pending payment reconciliation
    └── ...
```

---

## Features

### 🔐 Authentication & Authorization
- JWT token-based stateless authentication
- Role-based access control: **Admin** and **Customer** roles
- BCrypt password hashing
- Custom security filter chain with ordered filters

### 📦 Product & Catalog
- Full CRUD with multi-part image uploads via Cloudinary
- Category management with product associations
- Search and filtering through JPA Specifications

### 🛒 Shopping Experience
- Persistent user carts with quantity management
- Wishlist support for saved products
- Product review and rating system

### 💳 Payments & Checkout
- **Stripe Checkout Sessions** for PCI-compliant payment processing
- **Idempotency keys** to prevent duplicate charges
- **Webhook handling** for real-time payment event processing
- **Scheduled payment reconciliation** for pending transactions
- **Retry with exponential backoff** on transient Stripe failures

### 🎟️ Coupon System
- Admin-managed discount codes (percentage or fixed amount)
- Usage limits, expiration dates, minimum order validation
- Real-time coupon validation endpoint for clients

### 🔒 Reliability & Security
- **Rate limiting** per client IP
- **Correlation IDs** (X-Request-Id) for distributed tracing
- **Audit logging** on all requests
- **Optimistic locking** on all entities via `@Version`
- Structured error responses with typed `ErrorCode` classification

### 📊 Observability
- Spring Boot Actuator endpoints: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- Structured logging with request correlation

### 📖 API Documentation
- Interactive Swagger UI with custom dark theme
- Grouped API views: Public, Shopping, All
- Persistent authorization for testing authenticated endpoints

---

## Getting Started

### Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 21+ |
| Maven | 3.9+ |
| PostgreSQL | 17+ |
| Stripe Account | [dashboard.stripe.com](https://dashboard.stripe.com) |
| Cloudinary Account | [cloudinary.com](https://cloudinary.com) |

### 1. Clone the Repository

```bash
git clone https://github.com/Nsarkar-XLR8/SpringBoot_Ecom_API.git
cd SpringBoot_Ecom_API
```

### 2. Set Up PostgreSQL

```sql
CREATE DATABASE ecommerce_db;
CREATE USER ecommerce_user WITH PASSWORD 'ecommerce123';
GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ecommerce_user;
```

### 3. Configure Environment

Create `src/main/resources/application.properties` from the example:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Or set environment variables (see [Environment Variables](#environment-variables)).

### 4. Run the Application

```bash
# Using Maven Wrapper
./mvnw spring-boot:run

# Or using system Maven
mvn clean install -DskipTests
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### 5. Access Swagger UI

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser.

---

## Environment Variables

All configuration values have sensible defaults for local development. Override them via environment variables for staging/production:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC connection string | `jdbc:postgresql://localhost:5432/ecommerce_db` |
| `DB_USERNAME` | Database user | `ecommerce_user` |
| `DB_PASSWORD` | Database password | `ecommerce123` |
| `APP_JWT_SECRET` | HMAC signing key for JWT tokens | *(generated)* |
| `APP_JWT_EXPIRATION` | Token TTL in milliseconds | `86400000` (24h) |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | — |
| `CLOUDINARY_API_KEY` | Cloudinary API key | — |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | — |
| `STRIPE_SECRET_KEY` | Stripe secret key | — |
| `STRIPE_PUBLIC_KEY` | Stripe publishable key | — |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret | — |
| `STRIPE_CHECKOUT_SUCCESS_URL` | Post-payment redirect URL | `http://localhost:3000/checkout/success` |
| `STRIPE_CHECKOUT_CANCEL_URL` | Cancelled payment redirect URL | `http://localhost:3000/checkout/cancel` |
| `APP_RATE_LIMIT_REQUESTS_PER_MINUTE` | Rate limit threshold | `120` |
| `APP_CORS_ALLOWED_ORIGIN_PATTERNS` | CORS allowed origins | `http://localhost:*,https://*` |

---

## API Documentation

### Base URL

```
http://localhost:8080/api
```

### Response Envelope

All responses follow a consistent structure:

```json
{
  "code": "SUCCESS",
  "message": "Operation completed successfully",
  "data": { ... },
  "requestId": "a1b2c3d4-e5f6-7890",
  "timestamp": "2026-05-25T12:00:00"
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `SUCCESS` | 200 | Successful operation |
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `BUSINESS_ERROR` | 400 | Domain rule violation |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `DATA_INTEGRITY_ERROR` | 409 | Database constraint violation |
| `RATE_LIMITED` | 429 | Too many requests |
| `TRANSACTION_ERROR` | 500 | Transaction rollback |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

### Endpoint Groups

| Group | Base Path | Auth | Description |
|-------|-----------|------|-------------|
| **Auth** | `/api/auth` | Public | Registration, login |
| **Products** | `/api/products` | Public (GET) / Admin (CUD) | Product catalog CRUD |
| **Categories** | `/api/categories` | Public (GET) / Admin (CUD) | Category management |
| **Cart** | `/api/cart` | Customer | Shopping cart operations |
| **Checkout** | `/api/checkout` | Customer | Stripe checkout sessions |
| **Orders** | `/api/orders` | Customer / Admin | Order management |
| **Reviews** | `/api/products/*/reviews` | Public (GET) / Customer (POST) | Product reviews |
| **Wishlist** | `/api/wishlist` | Customer | Saved products |
| **Coupons** | `/api/coupons` | Admin (CRUD) / Customer (validate) | Discount codes |
| **Users** | `/api/users` | Customer | Profile management |
| **Admin** | `/api/admin` | Admin | Dashboard & analytics |
| **Webhooks** | `/api/webhooks/stripe` | Public (Stripe signature) | Payment event processing |

---

## Database Migrations

Database schema is managed by **Flyway**. Migrations are located at:

```
src/main/resources/db/migration/
```

Migrations run automatically on application startup. To run them manually:

```bash
./mvnw flyway:migrate
```

---

## Deployment

### Docker

```bash
# Build the image (multi-stage: Maven build → JRE runtime)
docker build -t ecommerce-api .

# Run with environment variables
docker run -d \
  --name ecommerce-api \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/ecommerce_db \
  -e DB_USERNAME=ecommerce_user \
  -e DB_PASSWORD=ecommerce123 \
  -e STRIPE_SECRET_KEY=sk_test_... \
  -e CLOUDINARY_CLOUD_NAME=... \
  ecommerce-api
```

### Production Checklist

- [ ] Set strong `APP_JWT_SECRET` (256-bit minimum)
- [ ] Use a managed PostgreSQL instance with SSL
- [ ] Configure Stripe webhook endpoint in the Stripe Dashboard
- [ ] Set `APP_CORS_ALLOWED_ORIGIN_PATTERNS` to your frontend domain(s)
- [ ] Enable HTTPS termination via reverse proxy (nginx / cloud LB)
- [ ] Set `spring.jpa.open-in-view=false`
- [ ] Review and tune rate limit thresholds for production traffic
- [ ] Connect Prometheus metrics to Grafana for monitoring

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is proprietary software. All rights reserved.

---

<p align="center">
  Built with ☕ and Spring Boot
</p>

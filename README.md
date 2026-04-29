# SpringBoot E-Commerce API (Professional Backend)

A high-performance, production-ready e-commerce backend built with **Spring Boot 3.5**, **Java 21**, and **PostgreSQL**. This project features full Stripe payment integration, Cloudinary image management, and advanced security architectures.

## 🚀 Key Features

- **Authentication & Security**:
  - JWT-based authentication with role-based access control (Admin/Customer).
  - Custom filters for **API Rate Limiting**, **Correlation IDs**, and **Audit Logging**.
- **Product & Category Management**:
  - Full CRUD operations with **Cloudinary** image uploads via `multipart/form-data`.
  - Advanced search and filtering using **JPA Specifications**.
- **Shopping Experience**:
  - Persisted user carts and persistent **Wishlists**.
  - Review and Rating system for products.
- **Payment & Order System**:
  - **Stripe Integration**: Secure checkout sessions, webhooks handling, and **idempotent transactions**.
  - **Concurrency Control**: Pessimistic locking in the database to prevent overselling of stock.
  - **Coupon System**: Admin-managed discount codes with usage limits and validation.
- **Reliability & Ops**:
  - **Flyway** for database schema versioning.
  - `@Retryable` logic for resilient communication with external APIs.
  - Automated payment validation scheduler for pending transactions.
  - **Swagger/OpenAPI 3.0** interactive documentation.

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.5 (Java 21)
- **Database**: PostgreSQL (JPA/Hibernate)
- **Security**: Spring Security, JJWT
- **Payment**: Stripe Java SDK
- **Media**: Cloudinary SDK
- **Build Tool**: Maven
- **Deployment**: Docker, Render

## ⚙️ Getting Started

### Prerequisites
- JDK 21
- Maven 3.x
- PostgreSQL
- Stripe & Cloudinary API Keys

### Configuration
Create a `.env` file or set environment variables for:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `APP_JWT_SECRET`
- `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`

### Run with Maven
```bash
mvn clean install
mvn spring-boot:run
```

### Run with Docker
```bash
docker build -t shop-api .
docker run -p 8080:8080 shop-api
```

## 📜 API Documentation
Once the app is running, access the interactive Swagger UI at:
`http://localhost:8080/swagger-ui.html`

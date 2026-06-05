# 🏨 Hotel Management System

A **production-ready** Hotel Management System built with Java 17, Spring Boot 3.2, Spring Security (JWT), Spring Data JPA, and MySQL.

---

## 📋 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Security | Spring Security 6 + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8.x |
| Build Tool | Maven 3.8+ |
| Utilities | Lombok, ModelMapper 3.1 |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Email | Spring Mail (Gmail SMTP) |
| Testing | JUnit 5, Mockito, H2 (test) |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/hotel/
│   │   ├── HotelManagementApplication.java
│   │   ├── config/
│   │   │   ├── ApplicationConfig.java    # Beans: AuthProvider, PasswordEncoder, ModelMapper
│   │   │   ├── SecurityConfig.java       # HTTP security, CORS, filter chain
│   │   │   ├── SwaggerConfig.java        # OpenAPI 3 configuration
│   │   │   └── AsyncConfig.java          # Thread pools for async email
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java
│   │   │   ├── RoomController.java
│   │   │   ├── BookingController.java
│   │   │   ├── PaymentController.java
│   │   │   ├── HotelServiceController.java
│   │   │   ├── EnquiryController.java
│   │   │   └── AdminController.java
│   │   ├── dto/
│   │   │   ├── request/                  # RegisterRequest, LoginRequest, BookingRequest …
│   │   │   └── response/                 # ApiResponse<T>, AuthResponse, BookingResponse …
│   │   ├── entity/                       # JPA entities + Enums
│   │   ├── exception/                    # Custom exceptions + GlobalExceptionHandler
│   │   ├── repository/                   # Spring Data JPA repositories
│   │   ├── security/                     # JwtService, JwtAuthenticationFilter
│   │   ├── service/                      # Service interfaces
│   │   │   └── impl/                     # Service implementations
│   │   └── util/                         # BookingReferenceGenerator, SecurityUtils …
│   └── resources/
│       ├── application.properties
│       ├── schema.sql                    # MySQL DDL
│       └── data.sql                      # Seed data
└── test/
    ├── java/com/hotel/
    │   ├── controller/AuthControllerTest.java
    │   └── service/RoomServiceTest.java, BookingServiceTest.java
    └── resources/application.properties  # H2 test config
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.x running locally
- Gmail account (for email notifications)

### 1. Clone & Configure

```bash
git clone <repo-url>
cd hotel-management
```

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-16-char-app-password
```

> **Gmail App Password**: Go to Google Account → Security → 2-Step Verification → App passwords

### 2. Build & Run

```bash
# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/hotel-management-1.0.0.jar
```

### 3. Load Seed Data

Run `src/main/resources/data.sql` in your MySQL client to insert sample admin user and 10 rooms.

### 4. Access

| Resource | URL |
|----------|-----|
| API Base | `http://localhost:8080/api` |
| Swagger UI | `http://localhost:8080/api/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api/v3/api-docs` |

---

## 🔑 Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@grandhotel.com` | `Admin@1234` |
| Customer | `rahul@example.com` | `Admin@1234` |

---

## 🔐 Authentication Flow

```
1. POST /auth/register or /auth/login
2. Receive: { accessToken, refreshToken }
3. Set header: Authorization: Bearer <accessToken>
4. On expiry: POST /auth/refresh-token  (Header: Refresh-Token: <refreshToken>)
```

---

## 📌 Key Features

### ✅ Security
- JWT stateless authentication
- Role-based authorization (`ROLE_ADMIN`, `ROLE_CUSTOMER`)
- BCrypt password encoding (strength 12)
- CORS configured for frontend
- Method-level security (`@PreAuthorize`)

### ✅ Business Logic
- Room availability checked against overlapping bookings
- Booking reference auto-generated (`BKyyyyMMddXXXXXX`)
- Transaction ID auto-generated (`TXNyyyyMMddHHmmssXXXXXXXX`)
- Advance payment auto-confirms booking
- Check-in updates room status to OCCUPIED
- Check-out sets room to CLEANING
- Cancellation validates date/status constraints
- Payment validates amount doesn't exceed balance

### ✅ Email Notifications (Async)
- Welcome email on registration
- Email verification link
- Booking confirmation with full details
- Booking cancellation
- Check-in / Check-out confirmation
- Payment receipt
- Enquiry acknowledgement + response

### ✅ Exception Handling
All exceptions return consistent `ApiResponse<T>`:
- `ResourceNotFoundException` → 404
- `DuplicateResourceException` → 409
- `RoomNotAvailableException` → 409
- `BookingException` → 400
- `PaymentException` → 400
- `UnauthorizedAccessException` → 403
- Validation errors → 400 with field-level messages

---

## 🧪 Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=RoomServiceTest
mvn test -Dtest=BookingServiceTest
mvn test -Dtest=AuthControllerTest
```

Tests use H2 in-memory database — no MySQL required.

---

## 🗄️ Database Schema Overview

```
users ──┬── bookings ──┬── payments
        │              └── hotel_services
        └── enquiries

rooms ──── bookings
         └── room_amenities
         └── room_images
```

---

## 📧 Email Configuration

For production, set env variables instead of hardcoding:
```properties
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
app.jwt.secret=${JWT_SECRET}
spring.datasource.password=${DB_PASSWORD}
```

---

## 🚢 Production Deployment

```bash
# Build production JAR
mvn clean package -DskipTests -Pprod

# Run with external config
java -jar hotel-management-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://prod-host:3306/hotel_db \
  --app.jwt.secret=<64-char-secret>
```

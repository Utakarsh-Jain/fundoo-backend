# Fundoo Notes - Advanced Backend Application

A feature-rich, secure, and scalable note-management backend built using **Java, Spring Boot, Spring Data JPA, MySQL, JWT, Redis, ActiveMQ, RabbitMQ, Spring Batch, and Spring AOP**.

Fundoo Notes is designed to demonstrate practical backend engineering concepts, including REST API development, authentication and authorization, asynchronous messaging, caching, background processing, Excel integration, and centralized application monitoring.

---

## 1. Project Overview

Fundoo Notes is a backend application that allows authenticated users to create, manage, organize, and retrieve their personal notes.

The application follows a layered architecture that separates API handling, business logic, persistence, security, and supporting infrastructure.

### Key Objectives

- Build secure RESTful APIs using Spring Boot.
- Implement user registration and authentication using JWT.
- Store and manage user-specific notes using MySQL.
- Implement note lifecycle operations such as pinning, archiving, and trashing.
- Introduce asynchronous event processing using JMS and RabbitMQ.
- Use Redis for OTP management and temporary data storage.
- Implement application-level caching.
- Process bulk Excel imports using Spring Batch and Apache POI.
- Implement centralized logging and performance monitoring using Spring AOP.

## 2. Technology Stack

| Category | Technology |
|---|---|
| Language | Java 22 |
| Backend Framework | Spring Boot 4.1.2 |
| Build Tool | Maven |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| API Development | Spring Web MVC |
| Authentication | JWT |
| Password Security | BCrypt |
| Validation | Jakarta Bean Validation |
| Messaging | ActiveMQ / JMS |
| Message Broker | RabbitMQ |
| In-memory Data Store | Redis |
| Caching | Spring Cache |
| Batch Processing | Spring Batch |
| Excel Processing | Apache POI |
| Logging & Monitoring | SLF4J / Spring AOP |
| API Testing | Postman |
| IDE | IntelliJ IDEA |
| Containerization | Docker |

## 3. Application Architecture

The application follows a layered architecture.

```text
                    CLIENT / POSTMAN
                           |
                           v
                    REST CONTROLLERS
                           |
                           v
                    REQUEST VALIDATION
                           |
                           v
                     SERVICE LAYER
                           |
              +------------+------------+
              |            |            |
              v            v            v
         REPOSITORY    JWT SERVICE   EVENT PUBLISHER
              |                         |
              v                  +------+------+
          MYSQL DB               |             |
                                 v             v
                              ActiveMQ      RabbitMQ

              +------------------------------+
              |
              v
          Redis / Cache

              |
              v
         Spring Batch
              |
              v
         Excel Processing

      Spring AOP surrounds service
      execution for logging and metrics.
```

### Layer Responsibilities

**Controller Layer**
- Exposes REST endpoints.
- Accepts client requests.
- Performs request-level validation.
- Returns appropriate HTTP responses.

**Service Layer**
- Contains business logic.
- Handles user authentication and note operations.
- Coordinates repository operations.
- Publishes messaging events.

**Repository Layer**
- Communicates with the MySQL database.
- Performs database queries using Spring Data JPA.

**Entity Layer**
- Represents database tables.
- Defines relationships between application entities.

**DTO Layer**
- Separates API request and response structures from database entities.
- Supports request validation.

**Configuration Layer**
- Contains application configuration for Redis, JMS, RabbitMQ, caching, and batch processing.

**Aspect Layer**
- Implements cross-cutting functionality such as method logging and execution-time measurement.

## 4. Implemented Features

### 4.1 User Management

- User registration.
- User login.
- Email-based user lookup.
- Password hashing using BCrypt.
- JWT generation after successful authentication.
- Token-based authentication for protected APIs.

### 4.2 Notes Management

- Create notes.
- Retrieve notes belonging to the authenticated user.
- Pin and unpin notes.
- Archive and unarchive notes.
- Move notes to trash and restore them.
- Verify note ownership before performing operations.

### 4.3 Global Exception Handling

- Centralized exception handling.
- Validation error handling.
- Consistent API error responses.
- Reduced repetitive exception-handling logic in controllers.

### 4.4 Asynchronous Messaging with JMS

Implemented using ActiveMQ and Spring JMS.

Features:
- Publish user-registration events.
- Serialize event payloads into JSON.
- Send messages to a dedicated queue.
- Consume messages using `@JmsListener`.

**Registration event flow:**

```text
User Registration
       |
       v
UserService
       |
       v
JmsTemplate
       |
       v
ActiveMQ Queue
       |
       v
JMS Consumer
       |
       v
Event Processing / Logging
```

The current consumer demonstrates event reception and logging. Actual email delivery is not configured yet.

### 4.5 RabbitMQ Integration

Implemented using Spring AMQP.

Features:
- Create reminder events.
- Configure durable queues.
- Configure direct exchanges.
- Configure routing keys and bindings.
- Publish reminder messages.
- Consume reminder events using a RabbitMQ listener.

**Reminder event flow:**

```text
Reminder API
     |
     v
Reminder Controller
     |
     v
Validate JWT and Note Ownership
     |
     v
RabbitMQ Producer
     |
     v
Direct Exchange
     |
     v
Routing Key
     |
     v
Reminder Queue
     |
     v
RabbitMQ Consumer
```

The current implementation demonstrates reminder event publishing and consumption. Actual email, SMS, or push notification delivery is not configured.

### 4.6 Redis Integration

Redis is used for temporary and short-lived application data.

Implemented functionality:
- Store key-value data.
- Retrieve stored values.
- Delete Redis entries.
- Check key existence.
- Configure key expiration.
- Generate six-digit OTPs.
- Store OTPs with configurable expiration.
- Verify OTPs and delete successfully verified OTPs.

### 4.7 Spring Cache

Implemented using Spring Cache abstraction.

Features:
- Enable application-level caching.
- Cache note retrieval results.
- Evict cached note data after note mutations.
- Reduce repeated database retrieval for cached reads.

The current configuration uses an in-memory simple cache.

### 4.8 Spring Batch

Implemented a Spring Batch job with:

- `Job`
- `Step`
- `ItemReader`
- `ItemProcessor`
- `ItemWriter`
- `JobRepository`
- `JobLauncher`

The batch job demonstrates reading records, processing them in chunks, and writing processed output.

### 4.9 Excel Integration

Implemented using Apache POI and Spring Batch.

Features:
- Upload Excel files using multipart requests.
- Read `.xlsx` files.
- Extract note title and description.
- Validate imported data.
- Convert Excel rows into Note entities.
- Associate imported notes with the authenticated user.
- Persist records using Spring Data JPA.
- Return batch execution information.

**Excel import flow:**

```text
Excel Upload
     |
     v
Excel Import Controller
     |
     v
JWT Validation
     |
     v
Spring Batch Job
     |
     v
ItemReader
     |
     v
ItemProcessor
     |
     v
ItemWriter
     |
     v
MySQL Database
```

Expected Excel columns:

| Title | Description |
|---|---|
| Project Planning | Prepare project milestones |
| Meeting Notes | Discussion and action items |
| Learning Goals | Topics to study |

### 4.10 Spring AOP

Implemented using Spring AOP.

**LoggingAspect**
- Logs service method execution.
- Logs service method completion.

**PerformanceAspect**
- Measures service method execution time.
- Records execution duration in milliseconds.
- Uses `ProceedingJoinPoint` and `@Around`.

Example log:

```text
Method: NoteServiceImpl.getAllNotes(..) | Execution Time: 110 ms
Completed service method: NoteServiceImpl.getAllNotes(..)
```

## 5. REST API Documentation

Base URL:

```text
http://localhost:8080
```

### User APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | Authenticate user and generate JWT |

Registration request:

```json
{
  "firstName": "Utkarsh",
  "email": "user@example.com",
  "password": "Password123"
}
```

Login request:

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

Use the actual DTO field names configured in the application.

### Notes APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/notes` | Create a note |
| GET | `/api/notes` | Retrieve authenticated user's notes |
| PATCH | `/api/notes/{id}/pin` | Toggle pin status |
| PATCH | `/api/notes/{id}/archive` | Toggle archive status |
| PATCH | `/api/notes/{id}/trash` | Toggle trash status |

Create note request:

```json
{
  "title": "Project Planning",
  "description": "Fundoo Notes development tasks"
}
```

### Reminder API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/reminders` | Publish a reminder event |

Request:

```json
{
  "noteId": 29,
  "reminderTime": "2026-11-17T18:30:00"
}
```

### Redis APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/redis/save?key={key}&value={value}` | Store Redis value |
| GET | `/api/redis/get/{key}` | Retrieve Redis value |
| DELETE | `/api/redis/delete/{key}` | Delete Redis value |
| POST | `/api/redis/otp/generate?email={email}` | Generate OTP |
| POST | `/api/redis/otp/verify?email={email}&otp={otp}` | Verify OTP |

**Note:** The OTP generation endpoint currently exposes the OTP for development and Postman testing. Production implementations should deliver OTPs through a secure notification channel instead.

### Batch API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/batch/run` | Execute demonstration batch job |
| POST | `/api/batch/import/notes` | Import notes from Excel |

Excel import requires a multipart form-data request:

```text
Key: file
Type: File
Value: your-notes.xlsx
```

### API Authentication

Protected APIs require a JWT access token.

Postman header:

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

Register and log in first to obtain a valid token.

## 6. Database Design

The application uses MySQL with Spring Data JPA.

### User Entity

| Field | Description |
|---|---|
| id | Primary key |
| firstName | User's first name |
| email | Unique user email |
| password | BCrypt-hashed password |

### Note Entity

| Field | Description |
|---|---|
| id | Primary key |
| title | Note title |
| description | Note content |
| pinned | Pin status |
| archived | Archive status |
| trashed | Trash status |
| user | Associated user |

### Relationship

```text
          USER
           |
           | 1
           |
           | 
           | N
          NOTE
```

A user can own multiple notes. Each note belongs to one user.

## 7. Prerequisites

Install the following before running the application:

- JDK 22
- Maven (or use the included Maven Wrapper)
- MySQL 8
- Docker Desktop
- IntelliJ IDEA
- Postman

Required infrastructure services:

| Service | Default Port |
|---|---:|
| Fundoo Notes | 8080 |
| MySQL | 3306 |
| ActiveMQ | 61616 |
| ActiveMQ Web Console | 8161 |
| RabbitMQ | 5672 |
| RabbitMQ Management UI | 15672 |
| Redis | 6379 |

## 8. Project Setup

### Step 1: Clone the repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

Navigate into the project:

```bash
cd fundoo-notes
```

### Step 2: Configure MySQL

Create the database:

```sql
CREATE DATABASE fundoo_notes;
```

Configure database credentials in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fundoo_notes
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Set the environment variable `DB_PASSWORD` to your local MySQL password.

### Step 3: Configure JWT

Set the following environment variable:

```text
JWT_SECRET=<YOUR_SECURE_JWT_SECRET>
```

The application uses:

```properties
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=86400000
```

Never commit actual passwords or JWT secrets to GitHub.

### Step 4: Start ActiveMQ

```bash
docker run -d --name fundoo-activemq \
-p 61616:61616 \
-p 8161:8161 \
apache/activemq-classic:6.1.7
```

ActiveMQ console:

```text
http://localhost:8161
```

Default development credentials:

```text
Username: admin
Password: admin
```

### Step 5: Start RabbitMQ

```bash
docker run -d --name fundoo-rabbitmq \
-p 5672:5672 \
-p 15672:15672 \
rabbitmq:4-management
```

Management console:

```text
http://localhost:15672
```

Default development credentials:

```text
Username: guest
Password: guest
```

### Step 6: Start Redis

```bash
docker run -d --name fundoo-redis \
-p 6379:6379 \
redis:7-alpine
```

### Step 7: Configure application.properties

Add the infrastructure configuration:

```properties
# ActiveMQ
spring.activemq.broker-url=tcp://localhost:61616
spring.activemq.user=admin
spring.activemq.password=admin
spring.jms.pub-sub-domain=false
app.jms.user-registration-queue=user.registration.queue

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

app.rabbitmq.reminder-queue=fundoo.reminder.queue
app.rabbitmq.reminder-exchange=fundoo.reminder.exchange
app.rabbitmq.reminder-routing-key=reminder.created

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=60000

app.redis.otp-expiration=300
app.redis.token-expiration=86400

# Spring Cache
spring.cache.type=simple

# Spring Batch
spring.batch.job.enabled=false
spring.batch.jdbc.initialize-schema=always
```

Preserve the remaining existing application properties and use the credentials configured for your environment.

### Step 8: Build the application

```powershell
.\mvnw.cmd clean compile
```

### Step 9: Run tests

```powershell
.\mvnw.cmd clean test
```

### Step 10: Run the application

Run `FundooNotesApplication` from IntelliJ IDEA or use:

```powershell
.\mvnw.cmd spring-boot:run
```

Application URL:

```text
http://localhost:8080
```

## 9. Testing with Postman

Recommended testing sequence:

1. Register a new user.
2. Log in and obtain JWT.
3. Create a note.
4. Retrieve all notes.
5. Pin a note.
6. Archive a note.
7. Move a note to trash.
8. Generate and verify an OTP.
9. Publish a reminder event.
10. Verify RabbitMQ consumer processing.
11. Verify JMS registration event processing.
12. Run the Spring Batch job.
13. Upload an Excel file.
14. Retrieve notes to verify imported records.
15. Observe AOP logging and execution-time output.

For protected endpoints, always use the JWT returned by the login API.

## 10. Configuration and Security Notes

- Do not store plain-text user passwords.
- Use BCrypt for password hashing.
- Keep JWT secrets outside source control.
- Validate JWTs before accessing protected resources.
- Verify note ownership before modifying user data.
- Use request DTO validation.
- Keep database access inside repository classes.
- Keep business logic inside service classes.
- Do not use cache as the primary data source.
- Use message brokers for asynchronous tasks.
- Avoid exposing development-only OTP values in production.
- Configure proper broker credentials and access controls before deployment.

## 11. Current Implementation Scope

The current project focuses on backend implementation and infrastructure integration.

| Module | Implementation |
|---|---|
| User Registration | Implemented |
| User Login | Implemented |
| BCrypt Password Hashing | Implemented |
| JWT Authentication | Implemented |
| Note CRUD Core | Implemented |
| Pin / Archive / Trash | Implemented |
| Global Exception Handling | Implemented |
| JMS Messaging | Implemented |
| RabbitMQ Integration | Implemented |
| Redis Key-Value Operations | Implemented |
| OTP Generation and Verification | Implemented |
| Spring Cache | Implemented |
| Spring Batch | Implemented |
| Excel Note Import | Implemented |
| AOP Logging | Implemented |
| AOP Performance Monitoring | Implemented |
| Actual Email Delivery | Not configured |
| Actual Reminder Notification Delivery | Not configured |
| Spring Cloud / Microservices | Not included |
| React Frontend | Planned separately |

## 12. Future Enhancements

Potential improvements for subsequent development:

- Forgot password and password reset workflows.
- Email verification and real email delivery.
- Reminder scheduling and actual notification delivery.
- Note search and filtering.
- Labels and categories.
- Note update and delete APIs.
- Attachment upload and retrieval.
- Redis-backed distributed caching.
- JWT logout and token invalidation.
- Failed message handling and retry mechanisms.
- Excel import failure reporting.
- Excel export.
- Automated integration testing.
- Production deployment and CI/CD.
- React frontend integration.

## 13. Learning Outcomes

This project demonstrates practical experience with:

- Java backend development.
- Spring Boot application architecture.
- REST API design and implementation.
- Authentication and authorization.
- Relational database integration.
- Asynchronous communication.
- Message queues and event-driven processing.
- Redis and application caching.
- Batch processing.
- Excel data integration.
- Aspect-Oriented Programming.
- Backend testing and debugging.
- Docker-based infrastructure setup.

## 14. Author

**Utakarsh Jain**


Project: Fundoo Notes - Advanced Backend Development

---


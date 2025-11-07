# Bank API

A simple banking REST API built to learn Spring Boot, Maven, and related technologies. This project implements basic banking operations with transaction management and a simple risk scoring system.

## Tech Stack

- Java 25
- Spring Boot 3.5.7
- PostgreSQL
- Maven
- Flyway (database migrations)
- MapStruct (DTO mapping)
- Lombok
- SpringDoc OpenAPI (Swagger)

## Features

### Accounts
- Create bank accounts with initial balance
- View account details and balance
- Update account status (ACTIVE, SUSPENDED, CLOSED)
- Generate unique account numbers

### Transactions
- Create debit/credit transactions
- Automatic risk scoring
- Transaction flagging based on risk threshold
- Transaction history per account
- Filter transactions by status, type, and date range

### Risk Scoring
The system calculates a risk score for each transaction based on:
- **Amount**: +30 points if > 10,000
- **Time**: +20 points if between 23:00 and 06:00
- **Frequency**: +40 points if more than 5 transactions in the last hour

Transactions with a risk score above 70 are automatically flagged as suspicious.

### Business Rules
- Debit transactions require sufficient balance
- Suspended accounts cannot perform transactions
- Transactions are automatically flagged if risk score > 70
- Account balance is updated only for completed transactions
- All amounts use 4 decimal precision

## API Endpoints

### Accounts
- `GET /api/v1/accounts` - List all accounts (paginated)
- `GET /api/v1/accounts/{id}` - Get account details
- `POST /api/v1/accounts` - Create a new account
- `PATCH /api/v1/accounts/{id}/status` - Update account status
- `GET /api/v1/accounts/{id}/transactions` - Get account transactions

### Transactions
- `GET /api/v1/transactions` - List all transactions (paginated, filterable)
- `GET /api/v1/transactions/{id}` - Get transaction details
- `POST /api/v1/transactions` - Create a new transaction
- `GET /api/v1/transactions/flagged` - List flagged transactions

## Getting Started

### Prerequisites
- Java 25
- Maven 3.6.3+
- PostgreSQL 12+

### Database Setup
Start a PostgreSQL instance:
```bash
docker run --name bank-postgres -e POSTGRES_DB=bank_db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=dev_passwd -p 5432:5432 -d postgres
```

### Running the Application
```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### API Documentation
Swagger UI is available at: `http://localhost:8080/swagger-ui`

OpenAPI JSON: `http://localhost:8080/api-docs`

### Running Tests
```bash
./mvnw test
```

Tests use Testcontainers to spin up a PostgreSQL instance automatically.

## Configuration

The application uses Spring profiles:
- `dev` (default): Development configuration with local PostgreSQL

Database configuration is in `src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bank_db
    username: admin
    password: dev_passwd
```

## Database Schema

The database is managed with Flyway migrations located in `src/main/resources/db/migration/`:
- V1: Create accounts table
- V2: Create transactions table
- V3: Add indexes
- V4: Add updated_at trigger for accounts

## Project Structure
```
src/
├── main/
│   ├── java/net/matheodrd/bankapi/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Request/Response DTOs
│   │   ├── exception/       # Custom exceptions
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   └── service/         # Business logic
│   └── resources/
│       ├── db/migration/    # Flyway migrations
│       └── application*.yml # Configuration files
└── test/                    # Unit and integration tests
```

## Example Requests

### Create an Account
```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountHolder": "John Doe",
    "initialBalance": 1000.00,
    "currency": "GBP"
  }'
```

### Create a Transaction
```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "023b710f-6bf2-482e-8b40-94d9ced11888",
    "amount": 50.00,
    "type": "DEBIT",
    "category": "PAYMENT",
    "description": "Coffee shop"
  }'
```

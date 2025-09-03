# Turno LOS Backend (Java 11 / Spring Boot 2.7.18)

Loan Origination System with: REST APIs, multithreaded background processing (safe locking), agent/manager assignment, mocked notifications, Flyway migrations, and a Postman collection.

## Prerequisites
- Java 11
- Maven 3.6+
- PostgreSQL 14+ (or use Docker Compose below)

Create DB:
```bash
createdb turno_los
psql -d turno_los -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;"
```

## Run
```bash
mvn spring-boot:run
```
Health: `http://localhost:8080/actuator/health`

## APIs
1. **Submit Loan**
   - `POST /api/v1/loans`
   - Body:
     ```json
     { "loanId":"L-1001", "customerName":"Riya", "customerPhone":"9876543210", "amount":150000, "type":"PERSONAL" }
     ```
2. **Agent Decision**
   - `PUT /api/v1/agents/{agentId}/loans/{loanPublicId}/decision`
   - Body: `{ "decision":"APPROVE" }` or `{ "decision":"REJECT" }`
3. **Status Counts**
   - `GET /api/v1/loans/status-count`
4. **Top Customers**
   - `GET /api/v1/customers/top?limit=3`
5. **Loans by Status (pagination)**
   - `GET /api/v1/loans?status=UNDER_REVIEW&page=0&size=10`

## Background processing
- Scheduler every 3s picks `APPLIED` loans using `FOR UPDATE SKIP LOCKED` and dispatches worker tasks to a thread pool.
- Each worker waits 10–25s, then applies simple rules:
  - Approve small amounts (≤ 200,000) → `APPROVED_BY_SYSTEM`
  - Invalid phone → `REJECTED_BY_SYSTEM`
  - Else → `UNDER_REVIEW` and assign the least-loaded available agent.
- Notifications are **mocked** (logs + `notifications` table).

## Postman
Import `postman/TurnoLOS.postman_collection.json`.

## Docker (optional)
```yaml
# docker-compose.yml
version: '3.8'
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_PASSWORD: postgres
      POSTGRES_USER: postgres
      POSTGRES_DB: turno_los
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata: {}
```
Run: `docker compose up -d`

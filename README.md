# Stark Group Vehicle Management System (VMS) — Backend

## Project Overview

The Stark Group Vehicle Management System (VMS) Backend is a Java Spring Boot application that provides REST APIs for managing company vehicles. It handles authentication, vehicle management, business logic, and database operations.

## Features

- User Authentication and Login
- Vehicle CRUD Operations
- Register New Vehicles
- View Vehicle List
- Update Vehicle Information
- Delete Vehicles
- Duplicate Vehicle Number Validation
- Input Validation and Error Handling
- Protected REST API Endpoints
- MySQL Database Integration

## Technology Stack

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Maven
- MySQL
- REST API

## API Endpoints

Base URL: `http://localhost:8080`

- `POST /api/v1/auth/login` — User Login
- `POST /api/v1/auth/logout` — User Logout
- `GET /api/v1/auth/me` — Get Current User
- `/api/v1/vehicles` — Vehicle Management

## Database

The application uses MySQL as its database.

Database name: `vms_db`

Database connection settings are configured through environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Do not commit database passwords or other secrets.

## Run Locally

### Prerequisites

- Java JDK
- MySQL Server
- Maven (or the included Maven Wrapper)

### Setup

1. Clone this repository.
2. Create a MySQL database named `vms_db`.
3. Configure the required database environment variables.
4. Open the project folder in a terminal.
5. Run the application:

```bash
.\mvnw.cmd spring-boot:run
```

The backend runs on port `8080` by default.

## Testing

Run the backend test suite using:

```bash
.\mvnw.cmd test
```

## Related Repository

Frontend: https://github.com/kabir-akib/vms-frontend

---

Developed as part of the Stark Group Vehicle Management System project.

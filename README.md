# Careermate

A Spring Boot application for career management and guidance.

## Prerequisites

- Java 17 or higher
- Maven
- Docker and Docker Compose
- PostgreSQL (if running locally without Docker)

## Getting Started

### Running with Docker Compose

1. Clone the repository
2. Navigate to the project root directory
3. Run the following command to start all services:
   ```bash
   docker compose up -d
   ```
   This will start:
   - PostgreSQL database (accessible on port 5432)
   - The Spring Boot application

4. To stop all services:
   ```bash
   docker compose down
   ```

### Running Locally (Without Docker)

1. Make sure you have PostgreSQL installed and running locally
2. Clone the repository
3. Navigate to the project root directory
4. Run the application using Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   For Windows users:
   ```bash
   mvnw.cmd spring-boot:run
   ```

## Database Configuration

The application uses PostgreSQL with the following default configuration:
- Database: mydatabase
- Username: myuser
- Password: secret

You can modify these settings in the `compose.yaml` file for Docker deployment or in `application.yml` for local deployment.

## Testing

To run the tests:
```bash
./mvnw test
```
For Windows users:
```bash
mvnw.cmd test
```

## Project Structure

```
src/
├── main/
│   ├── java/com/fpt/careermate/
│   │   ├── config/      # Application configuration
│   │   ├── domain/      # Domain entities
│   │   ├── repository/  # Data access layer
│   │   ├── services/    # Business logic
│   │   └── web/         # Controllers and REST endpoints
│   └── resources/
│       ├── application.yml
│       ├── static/
│       └── templates/
└── test/
    └── java/com/fpt/careermate/
```

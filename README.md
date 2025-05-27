# TED Talks Analysis Application

A Spring Boot application that analyzes TED Talks data to identify influential speakers and talks based on views, likes, and other metrics.

## Technologies

- Java 17+
- Spring Boot
- PostgreSQL
- Docker
- Swagger/OpenAPI
- Flyway for database migrations

## Prerequisites

- Docker and Docker Compose
- JDK 17 or newer
- Maven 3.6+

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd ted-talks-analysis
```

### 2. Start the PostgreSQL database with Docker

```bash
cd docker
docker-compose up -d
```

This will start a PostgreSQL instance accessible at `localhost:5432` with:
- Username: postgres
- Password: postgres
- Database: tedtalksdb

### 3. Build and run the application

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on port 8080.

## CSV Data Import

The application can automatically import TED Talks data from CSV files on startup:

- Place CSV files in the `src/main/resources/data` directory
- Import settings can be configured in `application.yaml`:
You can also upload CSV files via the API at `/api/v1/tedtalks/import`. You can use swagger UI to this.

```yaml
tedtalks:
  csv:
    import:
      enabled: true        # Enable/disable auto-import
      path: classpath:data # Directory containing CSV files
      batchSize: 100       # Batch size for large imports
```

## API Documentation

The API documentation is available via Swagger UI at:
- http://localhost:8080/swagger-ui.html

API specs are also available in JSON format at:
- http://localhost:8080/api-docs

## Key Endpoints

### Influence Analysis

- **GET /api/influence/speakers?limit=10**
    - Returns the top influential speakers based on views and likes
    - Optional `limit` parameter (default: 10)

- **GET /api/influence/yearly**
    - Returns the most influential TED Talk per year

## Configuration

The application's configuration is defined in `application.yaml`. Key configurations:

```yaml
tedtalks:
  influencer:
    viewsWeight: 0.7  # Weight for views in influence calculation
    likesWeight: 0.3  # Weight for likes in influence calculation
```

## Development Notes

- TED Talk data is stored in a PostgreSQL database
- Flyway is used for database migrations
- The application uses a weighted formula to calculate influence scores:
  `influenceScore = (views * viewsWeight) + (likes * likesWeight)`

## Testing

Run the tests with:

```bash
mvn test
```

Integration tests use TestContainers to spin up a temporary PostgreSQL container.
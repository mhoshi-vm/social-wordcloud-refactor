# CLAUDE.md

## Project Overview

Social Wordcloud is a multi-module microservices platform for social media analytics. It collects data from multiple sources (Mastodon, LinkedIn, NewsAPI, stock APIs), performs NLP/sentiment analysis using Spring AI, and visualizes results through an interactive web dashboard with word clouds, geographic maps, and stock charts.

**Organization:** `jp.broadcom.tanzu.mhoshi`

## Repository Structure

```
social-wordcloud-refactor/
├── analytics/       # Data processing & AI service (Spring Boot 4.0.0)
├── collector/       # Data ingestion from social sources (Spring Boot 4.0.0)
├── restapi/         # REST API layer with OpenAPI docs (Spring Boot 4.0.2)
├── graphql/         # GraphQL API with cursor pagination (Spring Boot 4.0.2)
├── log2gp/          # Fluentd-based log aggregation to Greenplum (Ruby)
└── frontend/        # Vanilla JS dashboard (D3, Leaflet, Chart.js)
```

Each Java module is an independent Maven project (no parent aggregator POM). They share no common parent beyond `spring-boot-starter-parent`.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.x, Spring Cloud 2025.1.0 |
| AI/ML | Spring AI 2.0.0-M1 (OpenAI integration) |
| Messaging | RabbitMQ via Spring Cloud Stream |
| RPC | gRPC via Spring gRPC 1.0.1, Protobuf 4.33.2 |
| Databases | H2 (local), PostgreSQL, Greenplum, MySQL |
| SQL Templating | MyBatis Thymeleaf |
| API Docs | SpringDoc OpenAPI 3.0.1 (restapi only) |
| Frontend | Vanilla JS, D3.js v7, Leaflet 1.9.4, Chart.js |
| Log Pipeline | Ruby / Fluentd |
| Deployment | Cloud Foundry |

## Build & Run

### Prerequisites
- Java 21
- Maven 3.x
- Docker (for Testcontainers-based integration tests)

### Build Commands

Each module is built independently from its own directory:

```bash
# Build a single module
cd analytics && mvn clean package
cd collector && mvn clean package
cd restapi && mvn clean package
cd graphql && mvn clean package

# Skip tests
cd <module> && mvn clean package -DskipTests
```

### Running Locally

All modules default to `spring.profiles.active=local` which uses H2 in-memory databases:

```bash
java -jar analytics/target/social-analytics-0.0.1-SNAPSHOT.jar
java -jar collector/target/social-collector-0.0.1-SNAPSHOT.jar
java -jar restapi/target/restapi-0.0.1-SNAPSHOT.jar
java -jar graphql/target/graphql-0.0.1-SNAPSHOT.jar
```

The frontend requires no build step -- open `frontend/index.html` in a browser. It polls `http://localhost:8080` by default.

## Testing

### Framework
- JUnit 5 (Jupiter)
- Spring Boot Test
- Testcontainers (PostgreSQL, MySQL, RabbitMQ, Greenplum)
- Spring Cloud Stream Test Binder

### Running Tests

```bash
# Run tests for a specific module
cd <module> && mvn clean test

# Run integration tests (requires Docker for Testcontainers)
cd <module> && mvn clean verify
```

### Test Organization
- Test classes use `@SpringBootTest` and Testcontainers for integration testing
- `TestContainersConfiguration.java` classes configure Docker containers per module
- The Spring Cloud Stream test binder replaces RabbitMQ in unit tests
- H2 is the default test database; Testcontainers provide PostgreSQL/MySQL/Greenplum for integration tests

## Module Architecture

### collector (Data Ingestion)
- Polls external APIs on configurable intervals via Spring Cloud Stream suppliers
- Sources: Mastodon (15s), NewsAPI (15min), LinkedIn (daily cron), Stocks (daily cron)
- Publishes `SocialMessage` events to RabbitMQ `output` destination
- Uses JPA with H2/MySQL for offset tracking
- Package: `jp.broadcom.tanzu.mhoshi.social.collector`

### analytics (Processing & AI)
- Consumes messages from RabbitMQ `output` destination
- Runs scheduled background jobs:
  - TF-IDF term frequency (15min)
  - VADER sentiment analysis (15min)
  - OpenAI embeddings generation (30min)
  - GIS coordinate guessing via AI (1hr)
  - Database maintenance (Saturday 1:15 AM)
- Exposes gRPC `Delete` service for message purging
- Uses Spring AI for OpenAI chat and embedding models
- SQL operations are DB-specific, stored under `src/main/resources/db/{h2,postgres,greenplum}/`
- Package: `jp.broadcom.tanzu.mhoshi.social.analytics`

### restapi (REST API)
- Serves three REST endpoint groups:
  - `/` - Social message analysis (with sentiment, clustering, GIS)
  - Term frequency data (day/week/month)
  - Stock price metrics
- SpringDoc OpenAPI UI available at `/swagger-ui.html`
- Uses Spring Data JDBC
- Package: `jp.broadcom.tanzu.mhoshi.social.restapi`

### graphql (GraphQL API)
- Relay-style cursor-based pagination (Connection/Edge/PageInfo pattern)
- Filtering by origin, lang, name with sort/direction support
- Calls analytics gRPC `Delete` service for mutations
- Schema at `src/main/resources/graphql/schema.graphqls`
- Package: `jp.broadcom.tanzu.mhoshi.social.graphql`

### frontend (Dashboard)
- `main.js` - Dashboard orchestration, 30-second API polling loop
- `widget-cloud.js` - D3-Cloud word cloud visualization
- `widget-map.js` - Leaflet geographic map with GeoJSON markers
- `widget-stock.js` - Chart.js stock price line charts

### log2gp (Log Aggregation)
- Ruby/Fluentd pipeline forwarding application logs to Greenplum
- Deployed via Cloud Foundry with Ruby buildpack

## Database Schema

The core data model centers on `social_message`:

| Table | Purpose |
|-------|---------|
| `social_message` | Core messages (PK: id + create_date_time) |
| `message_entity_sentiment` | VADER sentiment labels and scores |
| `message_entity_tsvector` | Text search word vectors |
| `vector_store` | OpenAI embedding vectors with JSONB metadata |
| `gis_info` | AI-guessed geographic coordinates |
| `social_message_analysis` | Denormalized analysis view (restapi) |
| `term_frequency_entity_{day,week,month}` | TF-IDF views by time range |
| `daily_stock_metrics` / `daily_stock_view` | Stock price aggregations |
| `hourly_message_stats` | Message count by origin per hour |

SQL schemas are duplicated per database dialect under `analytics/src/main/resources/db/{h2,postgres,greenplum}/`. When modifying schema, update all three dialect directories.

## Spring Profiles

| Profile | Database | Usage |
|---------|----------|-------|
| `local` (default) | H2 in-memory | Local development |
| `cf` | MySQL (collector) | Cloud Foundry deployment |
| `cf-greenplum` | Greenplum (analytics) | Cloud Foundry with distributed DB |

## Key Configuration Properties

### analytics `application.properties`
- `database` / `analytics.database` - Selects SQL dialect directory (`h2`, `postgres`, `greenplum`)
- `analytics.*-interval` - Millisecond intervals for scheduled processing jobs
- `analytics.maintenance-cron` - Cron expression for DB maintenance
- `spring.ai.openai.api-key` - OpenAI API key (empty by default, set via CredHub in CF)

### collector `application.properties`
- `spring.cloud.function.definition` - Pipe-separated list of active collector functions
- `spring.cloud.stream.bindings.*` - Poller intervals and destination bindings

## External Service Credentials

Managed via Cloud Foundry CredHub in production:
- `newsapi-key` - NewsAPI access key
- `mastodon-token` - Mastodon API token
- `apifylinkedin-token` - Apify LinkedIn scraping token
- `stocksapi-key` - Stock price API key
- OpenAI API key (via `spring.ai.openai.api-key` or CF GenAI service)

## Cloud Foundry Deployment

Each service has a `manifest.yml` in its module root:
- **analytics**: OpenJDK 21, 1GB memory, binds to `rabbitmq-broker`, `greenplum`, `on-prem-cpu`
- **collector**: OpenJDK 21, 1GB memory, binds to `rabbitmq-broker`, `offset-db`, `secret-store`
- **log2gp**: Ruby buildpack, 512MB memory

## Development Conventions

### Code Organization
- Standard Spring Boot layered architecture: Controller -> Service -> Repository
- One package per domain concern (e.g., `messages/`, `termfrequency/`, `stockprice/`)
- SQL files loaded via `FileLoader` utility; DB-dialect-specific SQL in `db/{dialect}/` directories
- gRPC protobuf definitions in `src/main/proto/`; generated code via `protobuf-maven-plugin`

### When Modifying
- **Adding a new data source**: Add a new supplier function pair in `collector`, wire it in `application.properties` function definitions and stream bindings
- **Changing DB schema**: Update SQL in all three dialect directories (`h2`, `postgres`, `greenplum`) under `analytics/src/main/resources/db/`; also update `restapi/src/main/resources/schema.sql` and `graphql/src/main/resources/schema.sql` if they reference affected tables
- **Adding REST endpoints**: Follow the existing Controller/Service/Repository pattern in `restapi`
- **Adding GraphQL fields**: Update `schema.graphqls`, then the corresponding `SocialMessageController` and repository
- **Modifying gRPC services**: Edit `.proto` files in `analytics/src/main/proto/`, then rebuild to regenerate Java stubs

### Important Notes
- There is no root aggregator POM; each module must be built separately
- No code style/linting tools are configured; follow existing code conventions
- No CI/CD pipeline is configured
- No `.gitignore` is present at the root level
- The `application-local.properties` files are excluded from JAR packaging but exist at build time
- Spring Boot 4.0.x uses Jakarta namespace (not javax)

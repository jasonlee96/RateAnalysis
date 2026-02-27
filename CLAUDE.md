# RateAnalysis - Claude Context

## Project Overview
Spring Boot 3.4.3 / Java 23 service that ingests, transforms, and monitors FX exchange rates.
Scheduled jobs handle ELT pipeline; a REST API allows manual rate ingestion.

## Tech Stack
- **Language**: Java 23
- **Framework**: Spring Boot 3.4.3
- **Build**: Maven (use `./mvnw`)
- **Database**: PostgreSQL — schema `ralysis`, migrations via Flyway (V1–V4)
- **ORM**: Hibernate / Spring Data JPA (physical naming strategy, no auto DDL)
- **Async**: `@Async` + `CompletableFuture` throughout
- **Scheduling**: `@Scheduled`

## Project Structure
```
src/main/java/com/smiley/
  main/           # Entry point (RateAnalysisApplication)
  controllers/    # REST API (RateController)
  services/       # Business logic (RateService, JobService, RateWatcherService)
  repositories/   # Data access with interfaces (Rate, JobInfo, RateSignal)
  jobs/           # Scheduled jobs (ELTLoadJob, ELTTransformJob, RateWatcherJob)
  entities/       # JPA entities (RateEntity, JobInfoEntity, RateSignalEntity)
  models/         # DTOs, enums, config models
  helpers/        # DateHelper, FileHelper
  common/         # AppSetting (@ConfigurationProperties)
src/main/resources/
  application.yml
  db/migration/   # Flyway scripts V1–V4
```

## Environment Variables
| Variable | Default | Required |
|----------|---------|----------|
| `DB_URL` | `jdbc:postgresql://postgres:5432/rate_analysis` | No |
| `DB_USERNAME` | `n8n` | No |
| `DB_PASSWORD` | — | **Yes** |

Never hardcode credentials. The password must always be injected via `${DB_PASSWORD}`.

## Key Domain Concepts

### Jobs (all run async, track state in `ralysis.jobinfos`)
| Job | Schedule | Purpose |
|-----|----------|---------|
| `ELTLoadJob` | every 30 min | Read CSV from `/data/rates/Rate_YYYYMMDD.csv`, insert into `rates` table |
| `ELTTransformJob` | every 30 min | Aggregate `rates` → daily/monthly tables. Depends on ELT_LOAD completing first |
| `RateWatcherJob` | every 5 min | Evaluate each configured symbol against thresholds, write to `rate_signals` |

### Job Status Flow
```
Pending(0) → Success(1) or Failed(2)
```
Jobs only run when `status != Pending` AND `nextJobAt <= now`.

### Symbols (configured in application.yml under `ranalysis.symbols`)
Each symbol has: `name`, `type`, `url`, `rateRegex`, and a `watcher` block.
The watcher compares `currentRate` against `hourlyAvg * threshold` and `dailyP90`.

### Signal Types (`RateSignalEnum`)
`NORMAL`, `HOURLY_HIGH`, `HOURLY_LOW`, `DAILY_HIGH`, `DAILY_LOW`

### REST API
- `POST /api/rates` — Ingest a single rate (body: `{ rate, dateRetrieved }`)
  - Inserts rate → updates ELT_LOAD job → triggers ELT transform

## Database Schema Summary
| Table | Purpose |
|-------|---------|
| `ralysis.rates` | Raw rate data (symbol, rate, dateRetrieved) |
| `ralysis.rates_agg_daily` | Daily aggregations (avg, p50, p90, min, max) |
| `ralysis.rates_agg_monthly` | Monthly aggregations |
| `ralysis.jobinfos` | Job scheduling/state control |
| `ralysis.rate_signals` | Watcher output signals |

## Configuration (`ranalysis.*`)
Loaded via `AppSetting.java` using `@ConfigurationProperties(prefix = "ranalysis")`.
- `batchsize: 50` — batch insert size
- `eltFilePath: /data/rates` — CSV input directory
- `eltFileNameFormat: Rate_{{DATE}}.csv` — filename template
- `symbols` — list of `SymbolConfig` (name, type, url, rateRegex, watcher settings)

## Known Issues / TODOs in Code
- `DateHelper.AddDays(LocalDate, int)` always adds 1 day regardless of the int argument (bug)
- `RateWatcherJob`: symbol `url` and `rateRegex` fields are configured but rate fetching is not yet implemented
- `ELTTransformJob`: job dependency check logic needs review
- Test coverage is minimal — only a Spring context smoke test exists
- Some catch blocks use `printStackTrace` instead of proper logging

## Git Workflow
- Active feature branch: `claude/understand-codebase-RM9xw`
- Main branch: `main`
- Always push to the `claude/` prefixed branch, never directly to `main`
- Use `git push -u origin <branch>` when pushing

## Build & Run
```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Run locally (requires DB_PASSWORD set)
DB_PASSWORD=yourpassword ./mvnw spring-boot:run

# Docker
docker build -t rate-analysis .
docker run -e DB_PASSWORD=yourpassword rate-analysis
```

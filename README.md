# OC Tools Backend Homework
**Jonathan Chang**  
chang.je@gmail.com

## Overview

This project implements a backend service that periodically fetches appliance data from a remote API, filters appliances based on operational status and last contact time, processes them by draining and remediating, logs the results, and exposes APIs to query processing logs.

The service is built with **Spring Boot** and uses scheduled tasks to run the main job every 5 minutes. It uses asynchronous processing using `CompletableFuture` and error handling with logging and persistence to an in memory data structure.

---
## Running the App
**Shell Script:**

This shell script runs the app and queries logs every 5 seconds. It writes app logs to app.log.
```bash
./start_app.sh
```
See app logs:
```bash
tail -f app.log
```

This shell script stops the app:
```bash
./stop_app.sh
```

## API cURL Examples

The service exposes the following REST endpoints to query appliance processing logs.

### Get all logs
```bash
curl -X GET "http://localhost:8080/api/logs" | jq
```
### Get all logs with pagination
```bash
curl -X GET "http://localhost:8080/api/logs?start=20&count=10" | jq
```
### Get logs for a specific appliance
```bash
curl -X GET "http://localhost:8080/api/logs/appliance/{appliance_id}?start=0&count=5" | jq
```
### Get recent logs (last 10 minutes), paginated
```bash
curl -X GET "http://localhost:8080/api/logs/recent?minutes=10&start=5&count=5" | jq
```
### Get failed logs, paginated
```bash
curl -X GET "http://localhost:8080/api/logs/failures?start=0&count=25" | jq
```
---
## Design

- **Structure:**  
  The project is organized into these classes:
    - `ApplianceProcessor` contains business logic to fetch, filter, process, and log appliances asynchronously.
    - `ApplianceLogController` exposes REST endpoints to query logs.
    - `ApiClient` handles interaction with the remote appliance API.

- **Scheduling:**  
  The main job runs on a schedule every 5 minutes using Spring's `@Scheduled` annotation. It also runs once immediately after startup (`@PostConstruct`).

- **Async Processing:**  
  Data fetching and appliance processing use `CompletableFuture` to perform concurrent requests.
  Only appliances with status `"LIVE"` and that have not reported in over 10 minutes (or never) are selected for draining and remediation. This filtering is done on the fetched data before processing.

- **Logging:**  
  Errors during fetch or processing are caught and logged. Processing failures are recorded in persistent logs (`ApplianceLogRepository`) with success/failure flags and timestamps.
  An in memory queue is used to store logs for simplicity.

- **API for querying Logs:**  
  The REST API provides endpoints to retrieve all logs, logs by appliance ID, recent logs within a time window, and only failed logs.

---
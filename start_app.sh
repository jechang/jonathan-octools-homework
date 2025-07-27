#!/bin/bash

# Step 1: Start the Spring Boot app in the background
echo "Starting app to query, filter, drain, and remediate appliances..."
echo "Logs are written to app.log"
./gradlew bootRun > app.log 2>&1 &

# Step 2: Wait for app to be ready (retry curl until success)
echo "Waiting for app to start..."
until curl -s 'http://localhost:8080/actuator/health' | grep -q '"status":"UP"'; do
  echo "App not ready yet, waiting 5 seconds..."
  sleep 5
done

echo "App is up! Starting to query logs every 5 seconds."

# Step 3: Query logs every 5 seconds
while true; do
  echo "Querying latest 5 appliance logs..."
  curl -s 'http://localhost:8080/api/logs?start=0&count=5' | jq .
  echo "Sleeping 5 seconds before querying again..."
  sleep 5
done
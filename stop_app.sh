#!/bin/bash

echo "Looking for Spring Boot app started with './gradlew bootRun'..."

# Find the PID of the app
APP_PID=$(ps aux | grep '[g]radlew bootRun' | awk '{print $2}')

if [ -z "$APP_PID" ]; then
  echo "No running Spring Boot app found."
else
  echo "Killing Spring Boot app with PID $APP_PID..."
  kill $APP_PID

  # Optionally, wait and force kill if not exiting
  sleep 2
  if ps -p $APP_PID > /dev/null; then
    echo "Process still running, sending SIGKILL..."
    kill -9 $APP_PID
  else
    echo "Process terminated successfully."
  fi
fi

echo "Stopping any running Gradle daemons..."
./gradlew --stop
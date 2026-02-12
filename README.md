
# CoreFlow Backend Deployment Guide

## 1. Clone and Prepare Source

```bash
cd /var/www/domains/coreflow.astraval.com/backend

# Clone the repository into 'source' folder
git pull
```

## 2. Build the Application

```bash
./gradlew clean build -x test
```

## 3. Deploy JAR

```bash
sudo cp /var/www/domains/coreflow.astraval.com/source/build/libs/coreflow-0.0.1-SNAPSHOT.jar /var/www/coreflow/coreflow.jar
```
```bash
# Restart the application after DB setup
sudo systemctl restart coreflow
sudo journalctl -u coreflow -f
```
## 4. log view
```bash
tail -f /logs/coreflow.log
tail -f /var/log/coreflow/coreflow.log
```
---
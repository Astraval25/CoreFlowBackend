
# CoreFlow Backend Deployment Guide

## 1. Clone and Prepare Source

```bash
cd /var/www/domains/coreflow.astraval.com
sudo rm -rf source

# Clone the repository into 'source' folder
sudo git clone https://github.com/Astraval25/CoreFlowBackend.git source

# Enter source folder
cd source

# Set ownership and permissions
sudo chown -R $USER:$USER .
sudo chmod -R 755 .
```

## 2. Build the Application

```bash
./gradlew clean build -x test
```

## 3. Deploy JAR

```bash

sudo cp /var/www/domains/coreflow.astraval.com/source/build/libs/coreflow-0.0.1-SNAPSHOT.jar /var/www/coreflow/coreflow.jar

# Set permissions
sudo chmod 755 /var/www/coreflow/coreflow.jar
sudo chown astraval:astraval /var/www/coreflow/coreflow.jar
```

```bash
# Restart the application after DB setup
sudo systemctl restart coreflow
sudo systemctl status coreflow
sudo journalctl -u coreflow -f
```
## 4. log view
```bash
tail -f /logs/coreflow.log
tail -f /var/log/coreflow/coreflow.log
```

```bash
sudo mkdir -p /logs
sudo chown -R astraval:astraval /logs
sudo chmod 755 /logs
```
---
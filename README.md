
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

## 5. Firebase Cloud Messaging (Android)

Set these environment variables before starting the app:

```bash
FIREBASE_ENABLED=true
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_CREDENTIALS_FILE_PATH=/absolute/path/firebase-service-account.json
```

Optional alternative:

```bash
FIREBASE_CREDENTIALS_JSON='{"type":"service_account", ... }'
```

### API Endpoints

1. Register device token (authenticated)

```http
POST /api/notifications/fcm/device-token
Content-Type: application/json
Authorization: Bearer <access_token>

{
  "fcmToken": "android_device_fcm_token"
}
```

2. Remove device token (authenticated)

```http
DELETE /api/notifications/fcm/device-token
Content-Type: application/json
Authorization: Bearer <access_token>

{
  "fcmToken": "android_device_fcm_token"
}
```

3. Send push notification (authenticated)

```http
POST /api/notifications/fcm/send
Content-Type: application/json
Authorization: Bearer <access_token>

{
  "title": "Order Update",
  "body": "Your order has been dispatched.",
  "userIds": [1, 2],
  "fcmTokens": [],
  "data": {
    "orderId": "12345",
    "type": "ORDER_STATUS"
  }
}
```
---

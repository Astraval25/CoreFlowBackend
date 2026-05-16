`Failed to initialize FirebaseApp: firebase-service-account-adminsdk-coreflow.json (No such file or directory)` means the backend process cannot find/read the Admin SDK JSON on the server.

Your app config currently expects:
- [application.yml](E:/workspace/CoreFlow/CoreFlowBackend/src/main/resources/application.yml:46)
- `app.firebase.service-account-path: ${FIREBASE_SERVICE_ACCOUNT_PATH:firebase-service-account-adminsdk-coreflow.json}`

So fix it by setting an **absolute path** in `FIREBASE_SERVICE_ACCOUNT_PATH`.

1. Copy key to server (safe location)
```bash
sudo mkdir -p /opt/coreflow/secrets
sudo cp coreflow-2026-04-firebase-adminsdk-fbsvc-64b2c3159a.json /opt/coreflow/secrets/firebase-service-account-adminsdk-coreflow.json
```

2. Set permissions for service user (replace `coreflow` if different)
```bash
sudo chown coreflow:coreflow /opt/coreflow/secrets/firebase-service-account-adminsdk-coreflow.json
sudo chmod 600 /opt/coreflow/secrets/firebase-service-account-adminsdk-coreflow.json
```

3. Set env var in systemd service
```bash
sudo systemctl edit --full coreflow
```
Add/update under `[Service]`:
```ini
Environment="FIREBASE_SERVICE_ACCOUNT_PATH=/opt/coreflow/secrets/firebase-service-account-adminsdk-coreflow.json"
```

4. Reload + restart
```bash
sudo systemctl daemon-reload
sudo systemctl restart coreflow
```

5. Verify
```bash
journalctl -u coreflow -n 200 --no-pager | grep -E "FirebaseApp|Failed to initialize FirebaseApp"
```
You want:
```text
FirebaseApp initialized successfully
```

If you don’t want env var, the alternative is placing the file in the service `WorkingDirectory` with exact name `firebase-service-account-adminsdk-coreflow.json`. Absolute path via env var is more reliable.
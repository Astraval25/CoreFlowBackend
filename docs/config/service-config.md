Your service file is missing `Environment=` lines, and that temp filename (`.#coreflow...`) means you may not be editing the real unit file yet.

Use this exact final unit content in `/etc/systemd/system/coreflow.service`:

```ini
[Unit]
Description=CoreFlow Spring Boot App
After=network.target

[Service]
User=root
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="FIREBASE_SERVICE_ACCOUNT_PATH=/opt/coreflow/secrets/firebase-service-account-adminsdk-coreflow.json"
ExecStart=/usr/bin/java -jar /var/www/coreflow/coreflow.jar --server.port=8085 --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Then run:

```bash
sudo systemctl daemon-reload
sudo systemctl restart coreflow
sudo systemctl show coreflow --property=Environment
sudo journalctl -u coreflow -n 120 --no-pager | grep -E "FirebaseApp|Failed to initialize FirebaseApp"
```

You should see non-empty `Environment=` and `FirebaseApp initialized successfully`.

Optional cleanup:
- You can remove `--spring.profiles.active=prod` from `ExecStart` since `SPRING_PROFILES_ACTIVE=prod` already sets it.
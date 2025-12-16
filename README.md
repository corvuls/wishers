# Wishers (simplified)

This is a simplified, stable Spring Boot + Thymeleaf version that keeps the existing dark iOS-like UI.

## Requirements
- Java 21
- Maven

## Run (Mac + iPhone)
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

mvn spring-boot:run
```

Open on Mac:
- http://localhost:8081/login

Open on iPhone (same Wi‑Fi as Mac):
```bash
ipconfig getifaddr en0
```
Then:
- http://<YOUR_MAC_WIFI_IP>:8081/login

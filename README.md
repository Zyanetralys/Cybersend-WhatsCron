# CyberSend — WhatsApp Message Scheduler

```
 ██████╗██╗   ██╗██████╗ ███████╗██████╗ ███████╗███████╗███╗   ██╗██████╗
██╔════╝╚██╗ ██╔╝██╔══██╗██╔════╝██╔══██╗██╔════╝██╔════╝████╗  ██║██╔══██╗
██║      ╚████╔╝ ██████╔╝█████╗  ██████╔╝███████╗█████╗  ██╔██╗ ██║██║  ██║
██║       ╚██╔╝  ██╔══██╗██╔══╝  ██╔══██╗╚════██║██╔══╝  ██║╚██╗██║██║  ██║
╚██████╗   ██║   ██████╔╝███████╗██║  ██║███████║███████╗██║ ╚████║██████╔╝
 ╚═════╝   ╚═╝   ╚═════╝ ╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝╚═╝  ╚═══╝╚═════╝
```

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-00ff41?style=flat-square)
![Status](https://img.shields.io/badge/status-active-00ff41?style=flat-square)
![GitHub Pages](https://img.shields.io/badge/GitHub_Pages-ready-181717?style=flat-square&logo=github)

**Schedule WhatsApp messages with precision. No bullshit, no subscriptions.**

[Demo](#-demo) · [Quick Start](#-quick-start) · [Architecture](#-architecture) · [API Reference](#-api-reference) · [Contributing](#-contributing)

</div>

---

## What is this?

CyberSend is a self-hosted WhatsApp message scheduler. You write a message, pick a time, and it fires — automatically, via WhatsApp Web.

It runs in two modes:

- **Standalone HTML** — drop `index.html` anywhere, including GitHub Pages. No server, no dependencies. The browser acts as the scheduler and opens WhatsApp Web at the right time using `wa.me` deep links.
- **Full backend (Spring Boot)** — persistent storage, REST API, real-time WebSocket updates, and a proper cron-based dispatcher. Connects to WhatsApp via session bridge or the official Business API.

The UI is intentionally overkill: Matrix rain, CRT scanlines, glitch typography. Because why not.

---

## Demo

Open `index.html` directly in your browser. Click **SIMULATE CONNECTION (DEMO)**, schedule a message, and watch the terminal log. No installation required.

For GitHub Pages: push `index.html` to your repo root, enable Pages from the repo settings, done.

---

## Quick Start

### Standalone (browser-only)

No setup. Just open the file:

```bash
open index.html
# or
python3 -m http.server 3000 && open http://localhost:3000
```

To deploy to GitHub Pages:

```bash
git init
git add index.html
git commit -m "init: deploy CyberSend"
git remote add origin https://github.com/youruser/cybersend.git
git push -u origin main
# Then: Settings → Pages → Deploy from branch → main → / (root)
```

Your app will be live at `https://youruser.github.io/cybersend`.

---

### Backend (Spring Boot)

**Requirements:** Java 17+, Maven 3.8+

```bash
git clone https://github.com/youruser/cybersend.git
cd cybersend
mvn spring-boot:run
```

Server starts at `http://localhost:8080`. Open `index.html`, set the **API Endpoint** field to `http://localhost:8080`, and click **TEST CONNECTION**. The frontend will switch to backend mode automatically.

To build a fat JAR for deployment:

```bash
mvn clean package -DskipTests
java -jar target/whatsapp-scheduler-1.0.0.jar
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    index.html (Browser)                  │
│                                                          │
│  ┌────────────────┐      ┌───────────────────────────┐  │
│  │  Local Mode    │      │      Backend Mode          │  │
│  │  (default)     │      │                           │  │
│  │                │      │  REST  ──▶  /api/*        │  │
│  │  setInterval   │      │  WS    ──▶  /ws (STOMP)   │  │
│  │  (15s checks)  │      │                           │  │
│  │       │        │      │                           │  │
│  │  wa.me links   │      │                           │  │
│  └────────────────┘      └───────────────────────────┘  │
└──────────────────────────────────┬──────────────────────┘
                                   │ HTTP / WebSocket
                    ┌──────────────▼──────────────────┐
                    │       Spring Boot 3.2            │
                    │                                  │
                    │  ApiController   (REST)          │
                    │  WebSocketConfig (STOMP broker)  │
                    │  MessageSchedulerService (@Scheduled 30s) │
                    │  WhatsAppService (session mgmt)  │
                    │                                  │
                    │  H2 (dev) / PostgreSQL (prod)    │
                    └──────────────┬───────────────────┘
                                   │
                    ┌──────────────▼───────────────────┐
                    │      WhatsApp Integration        │
                    │                                  │
                    │  Option A: wa.me deep links      │
                    │  Option B: whatsapp-web.js bridge│
                    │  Option C: Business API (Meta)   │
                    └──────────────────────────────────┘
```

The backend persists messages in H2 (file-based, survives restarts). A `@Scheduled` task runs every 30 seconds, picks up any `PENDING` messages whose `scheduled_at <= NOW()`, and dispatches them. Status transitions are pushed to connected clients via STOMP WebSocket on `/topic/message-updates`.

---

## WhatsApp Integration

CyberSend supports three integration strategies, ordered by complexity:

### wa.me deep links (zero setup)

The default fallback. Generates a `https://wa.me/{phone}?text={encoded}` URL and opens it in a new tab. Works on desktop and mobile. The user still needs to press Send manually — but combined with the scheduler, it opens WhatsApp Web at exactly the right moment.

No credentials, no API keys, no session management.

### whatsapp-web.js bridge (recommended for full automation)

Run a small Node.js sidecar that wraps [whatsapp-web.js](https://github.com/pedroslopez/whatsapp-web.js) and exposes a local REST endpoint. The Java service calls that endpoint to send messages without any user interaction.

```bash
cd bridge
npm install
node server.js
# Scan the QR code once — session is persisted to .wwebjs_auth/
```

The bridge exposes `POST /send` with `{ phone, message }`. Edit `WhatsAppService.java` to point at it:

```java
// WhatsAppService.java — callNodeBridge()
String url = "http://localhost:3001/send";
RestTemplate rt = new RestTemplate();
rt.postForEntity(url, Map.of("phone", phoneNumber, "message", message), String.class);
```

### WhatsApp Business API (production / high volume)

Requires a verified Meta Business account and a registered phone number. Set your token as an env variable and uncomment the relevant block in `WhatsAppService.java`:

```bash
export WHATSAPP_BUSINESS_TOKEN=your_token_here
export WHATSAPP_PHONE_NUMBER_ID=your_number_id
```

```java
// WhatsAppService.java
String url = "https://graph.facebook.com/v18.0/" + phoneNumberId + "/messages";
// ... standard Business API payload
```

Rate limits and pricing apply — check [Meta's documentation](https://developers.facebook.com/docs/whatsapp/cloud-api/get-started).

---

## Project Structure

```
cybersend/
│
├── index.html                              # Standalone frontend — deploy this to GitHub Pages
├── pom.xml
├── README.md
│
└── src/
    ├── main/
    │   ├── java/com/whatsapp/scheduler/
    │   │   │
    │   │   ├── WhatsAppSchedulerApplication.java   # Entry point, @EnableScheduling
    │   │   │
    │   │   ├── controller/
    │   │   │   └── ApiController.java              # All REST endpoints, CORS configured
    │   │   │
    │   │   ├── service/
    │   │   │   ├── WhatsAppService.java            # Session lifecycle, QR, send logic
    │   │   │   └── MessageSchedulerService.java    # @Scheduled dispatcher, recurrence
    │   │   │
    │   │   ├── model/
    │   │   │   ├── ScheduledMessage.java           # JPA entity (status, recurrence, timezone)
    │   │   │   ├── WhatsAppSession.java            # Session entity (QR, status, phone)
    │   │   │   ├── ScheduledMessageRepository.java # Custom @Query for due messages
    │   │   │   └── WhatsAppSessionRepository.java
    │   │   │
    │   │   └── config/
    │   │       └── WebSocketConfig.java            # STOMP broker, /ws endpoint, SockJS
    │   │
    │   └── resources/
    │       └── application.properties             # H2, JPA, server config
    │
    └── test/
        └── java/                                  # (add your tests here)
```

---

## Configuration

`src/main/resources/application.properties`:

```properties
# Switch to PostgreSQL for production
spring.datasource.url=jdbc:postgresql://localhost:5432/cybersend
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update

# Tune the dispatch interval (ms)
# Default: 30000 (30 seconds)
# For high-precision scheduling, lower to 5000
```

Add the PostgreSQL driver to `pom.xml` when switching:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## API Reference

All endpoints are under `/api`. CORS is open (`*`) by default — lock it down for production.

### Messages

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| `GET` | `/api/messages` | — | Returns all messages, ordered by `scheduled_at DESC` |
| `POST` | `/api/messages` | `ScheduledMessage JSON` | Creates a scheduled message |
| `POST` | `/api/messages/{id}/send-now` | — | Overrides schedule and dispatches immediately |
| `POST` | `/api/messages/{id}/cancel` | — | Marks as `CANCELLED`, skipped by dispatcher |
| `DELETE` | `/api/messages/{id}` | — | Cancels and removes |

**POST /api/messages — request body:**

```json
{
  "phoneNumber": "34612345678",
  "contactName": "Ana García",
  "messageContent": "Hola, recuerda la reunión a las 10.",
  "scheduledAt": "2025-06-15T09:55:00",
  "recurrence": "NONE",
  "timezone": "Europe/Madrid"
}
```

`recurrence` accepts: `NONE`, `DAILY`, `WEEKLY`, `MONTHLY`.

`status` is managed server-side: `PENDING → SENDING → SENT | FAILED`.

### Session

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| `GET` | `/api/session` | — | Current session status |
| `POST` | `/api/session/connect` | — | Generates QR, status → `QR_PENDING` |
| `POST` | `/api/session/confirm` | `{ "phoneNumber": "346..." }` | Confirms scan, status → `CONNECTED` |
| `POST` | `/api/session/disconnect` | — | Terminates session |

### Utilities

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/stats` | Counts by status (`total`, `pending`, `sent`, `failed`) |
| `GET` | `/api/health` | Health check (`{ status: "ONLINE", time: "..." }`) |
| `GET` | `/api/server-time` | Server time + timezone + available IANA zones |
| `GET` | `/api/wa-link?phone=346...&message=Hola` | Generates a `wa.me` deep link |

### WebSocket

Connect to `ws://localhost:8080/ws` (SockJS + STOMP).

Subscribe to:
- `/topic/message-updates` — fires on every message status change
- `/topic/session-status` — fires on session connect/disconnect/QR

---

## Message Lifecycle

```
PENDING ──── dispatcher picks up ──▶ SENDING ──── success ──▶ SENT
                                         │
                                         └── failure ──▶ FAILED
                                         └── no session ──▶ PENDING (wa.me fallback link emitted)

PENDING ──── user cancels ──▶ CANCELLED
```

Recurring messages: when a `SENT` message has `recurrence != NONE`, the dispatcher clones it with `scheduled_at` bumped by the recurrence interval. The clone starts as `PENDING`.

---

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl + Enter` | Schedule message |
| `Ctrl + Shift + Enter` | Send immediately |
| `Escape` | Close any open modal |

---

## Features

- Schedule messages to any WhatsApp number with exact date, time and timezone
- Recurrence: daily, weekly, monthly (auto-clones after dispatch)
- Real-time status updates via WebSocket — no polling
- Browser geolocation for timezone auto-detection
- Backend-optional: the HTML file is fully self-contained and works offline
- H2 embedded database with file persistence (no data loss on restart)
- Fallback `wa.me` links always available regardless of session state
- QR-based WhatsApp Web authentication flow (simulated in demo mode)
- Full CORS support for cross-origin frontend/backend deployments
- H2 console available at `/h2-console` in dev mode

---

## Notes on WhatsApp Automation

WhatsApp's Terms of Service prohibit unauthorized automation of personal accounts. The `wa.me` deep link approach is fully compliant — it just pre-fills the message and requires the user to press Send. Full automation via `whatsapp-web.js` operates in a grey area for personal use; for production/commercial use, the official Business API is the correct path.

This project is intended for personal productivity and developer tooling. Use responsibly.

---

<div align="center">
<sub>Built with Spring Boot 3.2 · Java 17 · Vanilla JS · No npm install required for the frontend</sub>
</div>

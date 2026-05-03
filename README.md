# CYBERSEND — WhatsApp Message Scheduler

```
 ██████╗██╗   ██╗██████╗ ███████╗██████╗ ███████╗███████╗███╗   ██╗██████╗
██╔════╝╚██╗ ██╔╝██╔══██╗██╔════╝██╔══██╗██╔════╝██╔════╝████╗  ██║██╔══██╗
██║      ╚████╔╝ ██████╔╝█████╗  ██████╔╝███████╗█████╗  ██╔██╗ ██║██║  ██║
██║       ╚██╔╝  ██╔══██╗██╔══╝  ██╔══██╗╚════██║██╔══╝  ██║╚██╗██║██║  ██║
╚██████╗   ██║   ██████╔╝███████╗██║  ██║███████║███████╗██║ ╚████║██████╔╝
 ╚═════╝   ╚═╝   ╚═════╝ ╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝╚═╝  ╚═══╝╚═════╝
```

> Cyberpunk WhatsApp Message Scheduler — Schedule transmissions across time

---

## 🚀 Quick Start

### Option A: Standalone HTML (GitHub Pages / No backend)

1. Open `index.html` in any browser — **zero dependencies**
2. Click **SIMULATE CONNECTION (DEMO)** to activate demo mode
3. Schedule messages → they auto-open WhatsApp Web at the right time
4. Deploy to GitHub Pages: push `index.html` to your repo and enable Pages

### Option B: Full Java Backend (Spring Boot)

#### Requirements
- Java 17+
- Maven 3.8+

#### Run

```bash
cd whatsapp-scheduler
mvn spring-boot:run
```

Server starts at `http://localhost:8080`

In the app, set **API Endpoint** to `http://localhost:8080` and click **TEST CONNECTION**.

---

## 🏗️ Architecture

```
Browser (index.html)
    │
    ├── Local Mode (default)
    │     └── Browser-native scheduler (setInterval)
    │           └── WhatsApp Web deep link (wa.me)
    │
    └── Backend Mode (Java Spring Boot)
          └── REST API (/api/*)
          └── WebSocket (/ws) — real-time updates
          └── H2 Database (persistent messages)
          └── Spring @Scheduled (30s check)
                └── WhatsApp Bridge (Node.js / Business API)
```

---

## 🔌 WhatsApp Integration Options

### 1. wa.me Deep Links (Built-in, no setup)
- Opens WhatsApp Web/App with pre-filled message
- Works on desktop and mobile
- No authentication needed

### 2. whatsapp-web.js (Recommended for automation)

```bash
# Install Node.js bridge
npm install whatsapp-web.js qrcode-terminal express
node bridge/server.js
```

Bridge exposes REST endpoint that the Java app calls.

### 3. Official WhatsApp Business API
- Requires Meta Business verification
- Set `WHATSAPP_BUSINESS_TOKEN` environment variable
- Edit `WhatsAppService.java` to call `graph.facebook.com/v18.0/`

---

## ✨ Features

- **⏰ Message Scheduling** — set exact date/time in any timezone
- **🔁 Recurrence** — daily / weekly / monthly repeating messages
- **📡 Real-time Updates** — WebSocket push for status changes
- **🌍 Timezone Support** — all IANA timezones
- **📍 Location Aware** — browser geolocation for smart scheduling
- **💾 Persistent Storage** — H2 embedded DB (swap for PostgreSQL)
- **⌨️ Keyboard Shortcuts** — Ctrl+Enter to schedule, Ctrl+Shift+Enter to send now
- **🎨 Cyberpunk UI** — Matrix rain, CRT scanlines, glitch effects
- **📱 WhatsApp Fallback** — wa.me deep links always work

---

## 🔑 Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl + Enter` | Schedule message |
| `Ctrl + Shift + Enter` | Send now |
| `Escape` | Close modals |

---

## 📁 Project Structure

```
whatsapp-scheduler/
├── index.html                          # Standalone frontend (GitHub Pages)
├── pom.xml                             # Maven build
└── src/main/java/com/whatsapp/scheduler/
    ├── WhatsAppSchedulerApplication.java
    ├── controller/
    │   └── ApiController.java          # REST endpoints
    ├── service/
    │   ├── WhatsAppService.java        # Session + sending logic
    │   └── MessageSchedulerService.java # Cron scheduler
    ├── model/
    │   ├── ScheduledMessage.java       # JPA entity
    │   ├── WhatsAppSession.java        # Session entity
    │   ├── ScheduledMessageRepository.java
    │   └── WhatsAppSessionRepository.java
    └── config/
        └── WebSocketConfig.java        # STOMP WebSocket
```

---

## 🌐 API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/messages` | List all messages |
| POST | `/api/messages` | Create scheduled message |
| POST | `/api/messages/{id}/send-now` | Send immediately |
| POST | `/api/messages/{id}/cancel` | Cancel message |
| GET | `/api/session` | Get WhatsApp session |
| POST | `/api/session/connect` | Initiate connection |
| POST | `/api/session/confirm` | Confirm QR scan |
| POST | `/api/session/disconnect` | Disconnect |
| GET | `/api/stats` | Message statistics |
| GET | `/api/health` | Health check |
| GET | `/api/wa-link?phone=&message=` | Generate wa.me link |

---

## 📦 GitHub Pages Deploy

```bash
# Just push index.html to your repo root
git add index.html
git commit -m "Deploy CyberSend"
git push origin main
# Enable GitHub Pages in Settings → Pages → main branch
```

---

*Built with Spring Boot 3.2 + Vanilla JS + Matrix aesthetics*

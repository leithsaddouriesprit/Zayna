🚍 Zayna - Intelligent School Transport Platform


A modern JavaFX + Spring Boot platform for managing and tracking school transportation in real time,
combining AI-driven assistance, operational management, and live geolocation.


---

## ✨ Overview

**Zayna** is a multi-role intelligent transport system designed for:

- 👨‍👩‍👧 Parents → tracking & monitoring
- 🧑‍🔧 Agents → operational management
- 🧑‍💼 Admins → global supervision
- 🤖 AI → smart querying + action execution

Built with a **desktop-first architecture**, enhanced by an **AI backend**, and powered by a **real-time tracking system**.

---

## 🚀 Features

### 👨‍👩‍👧 Parent Experience
- Real-time bus tracking (map + live position)
- ETA (Estimated Time of Arrival)
- Child on-board status
- AI assistant for tracking insights
- Child candidature management (create / follow / update)
- Dashboard with calendar & weather widgets

---

### 🧑‍🔧 Agent Experience
- School-scoped dashboard
- Full transport management:
  - buses
  - trajets (routes)
  - arrêts (stops)
  - children
  - chauffeurs
  - maitresses
- AI assistant for:
  - data queries (lists, counts, status)
  - operational commands (approve, assign, etc.)
- Access control based on candidature approval

---

### 🧑‍💼 Admin Experience
- Global system dashboards:
  - users by role
  - schools / buses / trajets
  - pending candidatures
  - reclamations
- Full management interfaces (agents, parents, transport, etc.)

---

### 🚍 Transport & Tracking
- Real-time bus tracking via Leaflet (JavaFX WebView)
- Route rendering from stops
- OSRM road routing integration
- ETA & distance computation
- Route simulation support
- Auto-refresh tracking (2 seconds)

---

## 🤖 AI Assistant (Core System)

Zayna integrates a **powerful AI agent system**, not just a chatbot.

---

### 🧠 Parent AI (`/ai/chat`)
- Context-aware tracking assistant
- Uses real-time data:
  - child
  - bus
  - tracking snapshot
- Supports:
  - location queries
  - ETA
  - safety status
  - child status
- Uses **deterministic answers first**, then controlled AI fallback

---

### 🧑‍🔧 Agent / Admin AI (`/ai/agent/chat`)

#### 🔍 Query Capabilities
- List & count:
  - chauffeurs
  - maitresses
  - buses
  - trajets
  - children
- Status checks and relationships
- School-scoped queries (secure)

#### ⚡ Action Capabilities
- Approve / reject chauffeur candidature
- Approve / reject child candidature
- Assign / unassign maitresse to bus

#### 🚫 Safety Rules
- Destructive operations (delete/remove) are blocked
- All actions are scoped to agent’s school

---

## 🔐 AI Execution Flow

### Step-by-step:
1. User sends request
2. AI detects intent
3. If action → returns proposal + `pendingActionId`
4. User confirms (UI: Confirm / Cancel)
5. Backend validates:
   - user
   - school scope
   - expiration (TTL)
6. Action executed safely (SQL)

---

### 🛡️ Anti-Hallucination System
- Deterministic answers when possible
- Context validation
- Strict DB-based responses
- Low-temperature AI configuration

> ❗ Rule: AI uses ONLY real system data (no invention)

---

## 🏗️ Architecture
📂 Project Structure
Zayna/
├─ src/main/java/tn/esprit/workshop/
│  ├─ controlleurs/           # JavaFX controllers (parent, agent, admin, tracking, AI UI)
│  ├─ services/               # JDBC/business services
│  ├─ model/                  # Domain models
│  └─ utilis/                 # Session, DB connection, app bootstrap
├─ src/main/resources/
│  ├─ leith/, Talel/, amal/   # FXML views, CSS, map assets
│  └─ db/                     # SQL migration scripts
├─ pom.xml                    # Desktop Maven module
└─ zayna-ai-api/
   ├─ src/main/java/tn/esprit/workshop/ai/
   │  ├─ controller/          # Parent AI endpoints
   │  ├─ agent/               # Agent AI (intents, actions, confirmation flow)
   │  ├─ service/             # Context, deterministic answers, prompt/response pipeline
   │  └─ dto/                 # API contracts
   ├─ src/main/resources/application.properties
   └─ pom.xml                 # Spring Boot AI API module


---

## 🗺️ Tracking System

- Leaflet + OpenStreetMap
- OSRM for road routing
- WebView integration
- Features:
  - live bus marker
  - route polyline
  - ETA calculation
  - next stop detection

---

## 🛠️ Tech Stack

### Desktop
- Java 17
- JavaFX
- JDBC
- MySQL Connector/J
- OkHttp / HttpClient
- JWT / BCrypt

### AI API
- Spring Boot 3
- Spring Web + JDBC
- Ollama (LLM)

### Mapping
- Leaflet
- OpenStreetMap
- OSRM API

---

## ⚙️ Installation

### 1. Prerequisites
- JDK 17
- Maven 3.9+
- MySQL 8+
- (Optional) Ollama

---

### 2. Database
use zaynaa.sql

📈 Future Improvements
Persist AI actions (Redis / DB)
Unified ETA system
Add integration tests
Externalize configs (env-based)
Mobile version (Flutter)   

👨‍💻 Authors
Leith Saddouri
Zayna Team

## 📄 License

This project is the intellectual property of **Leith Saddouri**.

It is developed for academic and educational purposes.  
Unauthorized use, reproduction, or distribution is not permitted without explicit permission.

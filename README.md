# Mini AI Health Coach

A chat-based mini AI health coach built for the **Curelink Backend Engineer Take-Home**. Simulates a WhatsApp-like single-session chat where an AI health coach answers user questions using a real LLM API, with support for context overflow handling, long-term memory, and protocol-based responses.

> **Assignment brief:** [Curelink-Backend-Engineer-Take-Home.md](./Curelink-Backend-Engineer-Take-Home.md)

---

## How to Run Locally (Step by Step)

1. **Prerequisites**
   - Java 21+
   - Maven 3.6+
   - (Optional) Node.js for frontend, if applicable

2. **Clone and enter the repo**
   ```bash
   git clone <repository-url>
   cd dattri
   ```

3. **Configure environment**
   - Copy `.env.example` to `.env` (or set env vars as in [Environment variables](#environment-variables)).
   - Set your LLM API key and DB/cache config.

4. **Start MySQL and Redis (local)**
   - **MySQL:** Ensure MySQL is running on `localhost:3306`. Create the DB:
     ```sql
     CREATE DATABASE IF NOT EXISTS curelink;
     ```
   - **Redis:** Ensure Redis is running on `localhost:6379` (e.g. `redis-server` or Docker).
   - Default config: user `root`, no password (see [Environment variables](#environment-variables)).

5. **Schema**
   - JPA is configured with `spring.jpa.hibernate.ddl-auto=update`, so tables are created/updated on first run. No separate migration step required for local dev.

6. **Start the backend**
   ```bash
   ./mvnw spring-boot:run
   ```

7. **Start the frontend (if separate)**
   ```bash
   cd frontend && npm install && npm run dev
   ```

8. **Open the app**
   - Backend API: `http://localhost:8080` (or port in `application.properties`)
   - Frontend: `http://localhost:3000` (or port used by your frontend)

---

## Database Setup (Migrations / Seed)

JPA is configured with `ddl-auto=update` so tables are auto-created on first boot. For manual setup or production, run the SQL below against the `curelink` database.

### Schema (CREATE TABLE)

```sql
CREATE DATABASE IF NOT EXISTS curelink;
USE curelink;

-- One row per user / API key. External session id links the key to the conversation.
CREATE TABLE IF NOT EXISTS chat_session (
    id         VARCHAR(36)  NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_chat_session_session_id (session_id)
);

-- Every chat message (user or assistant) in a session.
-- Indexed on session_id + created_at for efficient pagination.
CREATE TABLE IF NOT EXISTS chat_message (
    id         VARCHAR(36) NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    role       VARCHAR(20) NOT NULL COMMENT 'USER | ASSISTANT',
    content    TEXT        NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_chat_message_session_created (session_id, created_at),
    CONSTRAINT fk_chat_message_session
        FOREIGN KEY (session_id) REFERENCES chat_session (id)
        ON DELETE CASCADE
);

-- Long-term memory per session (extracted facts, user preferences, health context).
CREATE TABLE IF NOT EXISTS user_memory (
    id         VARCHAR(36) NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    content    TEXT        NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_user_memory_session (session_id),
    CONSTRAINT fk_user_memory_session
        FOREIGN KEY (session_id) REFERENCES chat_session (id)
        ON DELETE CASCADE
);

-- Health protocols / guidelines (e.g. fever, stomach ache, refund policy).
-- Matched with user queries at runtime and injected into the LLM prompt.
CREATE TABLE IF NOT EXISTS protocol (
    id         VARCHAR(36)  NOT NULL,
    code       VARCHAR(100) NOT NULL,
    title      VARCHAR(255),
    content    TEXT         NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_protocol_code (code)
);
```

### Seed protocols (optional)

```sql
INSERT IGNORE INTO protocol (id, code, title, content, created_at) VALUES
    (UUID(), 'FEVER',         'Fever Protocol',         'For mild fever (<38.5°C): rest, hydrate, paracetamol if needed. Escalate to doctor if >39°C or lasting >3 days.', NOW()),
    (UUID(), 'STOMACH_ACHE',  'Stomach Ache Protocol',  'For mild stomach pain: light diet, hydration, avoid spicy food. Escalate if pain is severe or persistent >24h.',   NOW()),
    (UUID(), 'HEADACHE',      'Headache Protocol',      'For tension headache: rest, hydrate, paracetamol. Escalate if sudden/severe or accompanied by vision changes.',       NOW()),
    (UUID(), 'REFUND_POLICY', 'Refund Policy',          'Customers can request a refund within 7 days of purchase. Subscriptions are non-refundable after 48 hours.',          NOW());
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values. `.env` is gitignored and never committed.

```bash
cp .env.example .env
```

| Variable | Required | Default | Description |
| -------- | -------- | ------- | ----------- |
| `OPENAI_API_KEY` | **Yes** | — | OpenAI API key (`sk-...`) |
| `DB_URL` | No | `jdbc:mysql://localhost:3306/curelink?...` | Full JDBC URL |
| `DB_USERNAME` | No | `root` | MySQL username |
| `DB_PASSWORD` | No | _(empty)_ | MySQL password |
| `DB_POOL_MIN_IDLE` | No | `2` | HikariCP minimum idle connections |
| `DB_POOL_MAX_SIZE` | No | `10` | HikariCP maximum pool size |
| `REDIS_HOST` | No | `localhost` | Redis host |
| `REDIS_PORT` | No | `6379` | Redis port |
| `REDIS_PASSWORD` | No | _(empty)_ | Redis password |
| `APP_AUTH_API_KEY` | No | _(empty = any key accepted)_ | API key clients must send in `X-API-Key` |
| `OPENAI_MODEL` | No | `gpt-4o` | OpenAI model name |
| `OPENAI_MAX_TOKENS` | No | `1000` | Max tokens per LLM response |
| `OPENAI_TEMPERATURE` | No | `0.7` | LLM temperature |
| `OPENAI_TIMEOUT_SECONDS` | No | `30` | HTTP timeout for OpenAI calls |
| `OPENAI_SKIP_SSL` | No | `false` | Set `true` locally if JVM cacerts is missing OpenAI's CA |
| `JPA_DDL_AUTO` | No | `update` | Hibernate DDL mode (`update` / `validate` / `none`) |
| `JPA_SHOW_SQL` | No | `false` | Log SQL statements |

---

## Running & Deployment

### Local dev (using `.env` file)

```bash
# Load .env and run
export $(grep -v '^#' .env | xargs) && ./mvnw spring-boot:run
```

### Local dev (inline env vars)

```bash
OPENAI_API_KEY=sk-... \
DB_PASSWORD=secret \
OPENAI_SKIP_SSL=true \
./mvnw spring-boot:run
```

### Production JAR (e.g. on a Linux VM / Render / Railway)

```bash
# 1. Build
./mvnw clean package -DskipTests

# 2. Run with all required env vars
OPENAI_API_KEY=sk-... \
DB_URL=jdbc:mysql://<host>:3306/curelink?useSSL=true&serverTimezone=UTC \
DB_USERNAME=<user> \
DB_PASSWORD=<pass> \
REDIS_HOST=<redis-host> \
REDIS_PASSWORD=<redis-pass> \
APP_AUTH_API_KEY=<your-api-key> \
JPA_DDL_AUTO=update \
OPENAI_SKIP_SSL=false \
java -jar target/dattri-0.0.1-SNAPSHOT.jar
```

### Docker (one-liner)

```bash
docker run --rm \
  -e OPENAI_API_KEY=sk-... \
  -e DB_URL=jdbc:mysql://<host>:3306/curelink \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secret \
  -e REDIS_HOST=<redis-host> \
  -e APP_AUTH_API_KEY=my-key \
  -p 8080:8080 \
  dattri:latest
```

---

## Architecture Overview

### Backend structure

- **Layers / modules:** (e.g. `controller` → `service` → `repository`, plus `llm`, `memory`, `protocols` if used.)
- **Design decisions:** (e.g. single prompt vs multi-prompt, how context window is managed, how long-term memory and protocols are injected.)

*(Fill in a short description of your package layout and main design choices.)*

### High-level flow

1. User sends a message via chat API.
2. Backend loads recent conversation, long-term memory, and relevant protocols.
3. Context is trimmed/prioritized to fit LLM limits.
4. LLM is called; response is persisted and returned.
5. Optional: typing indicator / history pagination APIs as needed for the frontend.

*(Adjust to match your implementation.)*

---

## LLM Notes

- **Provider:** (e.g. OpenAI GPT-4, Anthropic Claude, Google Gemini.)
- **Prompting:**
  - System prompt: (e.g. role as health coach, safety disclaimers, format.)
  - How onboarding and long-term memory are included.
  - How protocols (e.g. fever, stomach ache, refund) are selected and injected.
- **Context handling:** (e.g. sliding window, summarization, or last N messages + memory summary.)

*(Replace with your actual provider and prompting strategy.)*

---

## Trade-offs & “If I had more time…”

- **Trade-offs:** (e.g. simplicity vs. robustness, single prompt vs. multi-step agent, in-memory vs. Redis for session cache.)
- **If I had more time:** (e.g. proper RAG for protocols, finer-grained memory, rate limiting, tests, monitoring.)

*(Add a short, honest summary of what you chose and what you’d do next.)*

---

## API Overview & cURL Examples

All chat endpoints require API key authentication. Use **`X-API-Key`** or **`Authorization: Bearer <key>`** (or **`Authorization: ApiKey <key>`**). When `app.auth.api-key` is empty (local dev), any non-empty key is accepted. Base URL: `http://localhost:8080`.

### Send a message

```bash
curl -X POST http://localhost:8080/api/chat/messages \
  -H "X-API-Key: my-local-key" \
  -H "Content-Type: application/json" \
  -d '{"content":"Hello, I have a mild headache."}'
```

### Get messages (latest, or “load more” with cursor)

```bash
# Latest messages (e.g. first page, newest first)
curl "http://localhost:8080/api/chat/messages?limit=20" \
  -H "X-API-Key: my-local-key"

# Load more (older messages) using message id as cursor
curl "http://localhost:8080/api/chat/messages?limit=20&before=<message-id>" \
  -H "X-API-Key: my-local-key"
```

### Typing indicator

```bash
curl "http://localhost:8080/api/chat/typing" \
  -H "X-API-Key: my-local-key"
```

### Summary

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/chat/messages` | Send user message; returns assistant reply. |
| `GET` | `/api/chat/messages?limit=&before=` | List messages (newest first); `before` = cursor for “load more”. |
| `GET` | `/api/chat/typing` | Whether the coach is currently generating a reply. |

---

## License

MIT (or your choice).

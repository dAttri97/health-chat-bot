# Backend Engineer Take‑Home Test

## Context

For this assignment, you'll build a mini AI health coach on chat. This is very close to what you'd work on as a backend engineer at Curelink.

---

## Tech Stack (Guidelines, not hard rules)

We'd prefer you use some/all of:


| Layer    | Technology                                   |
| -------- | -------------------------------------------- |
| Backend  | Java SpringBoot                              |
| Frontend | NodeJS (emphasis on backend access patterns) |
| Database | MySQL                                        |
| Cache    | Redis (optional, nice-to-have)               |


If not comfortable with anything above, use whatever lets you move quickly and write clean code.

---

## The Assignment: Mini AI Health Coach

### Goal

Build a small web app that simulates a chat where an AI health coach answers user questions using a **real LLM API** (OpenAI / Gemini / etc.). The chat will be a **single session**, like a WhatsApp chat, and the user won't have an option to start or delete a session. We need to handle **context overflow automatically**.

### Core User Flow

1. The user opens a web page with a chat input and old chat history.
2. The old chat history should **automatically load on scrolling upwards**.
3. When first opened, ideally the agent should do some kind of **information gathering** or a small **onboarding** to gather more context on the user and get to know them on chat (might need updating a section of the prompt dynamically after the first session ends).
4. When a user sends a message, the backend should generate a reply to their query based on:
  - The recent chat
  - Some custom protocols
  - Even long-term memory which the agent might have for that specific user
5. Aim for a **WhatsApp-like user experience**, not an AI chat app like ChatGPT. The user should feel like they are interacting with a real person on WhatsApp.

### Functional Requirements

- **Chat interface** can be very simple, nothing fancy required. Think deeply about what APIs would be needed by the frontend to handle:
  - Autoscrolling to the latest message
  - Loading previous chat
  - Typing indicators
  - Other common chat features  
  Think deeply about the **access patterns** needed to support such common chat features.
- **All message history** should be persisted in the database.
- **Call a real LLM API** to generate the reply. It's up to you how to handle the LLM call architecture:
  - Do you need multiple prompts or a single prompt?
  - How to handle context overflow and capping maximum tokens?
  It's all up to you.
- Apart from the raw messages as context, handle **two types of other contexts**:
  1. **Older long-term memories** for the user
  2. **Common protocols** on handling medical situations (e.g. fever, stomach ache) or refund policies etc., which need to be **matched with the user queries in real-time**
- The code should be **robust and idiot-proof** to handle bad inputs, large inputs, and other edge cases that can occur in the real world.
---

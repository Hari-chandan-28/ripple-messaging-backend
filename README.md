# Ripple

A full-stack real-time messaging backend built with Spring Boot and Kotlin. Ripple supports user authentication, friend connections, group chats, and real-time messaging via a custom WebSocket protocol — built without STOMP or any messaging middleware.

---

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Kotlin |
| Framework | Spring Boot 4.1.0 |
| Database | MySQL 8 |
| ORM | Hibernate / Spring Data JPA |
| Migrations | Liquibase |
| Real-time | Raw WebSocket (Spring WebSocket) |
| Auth | JWT (Bearer Token) |
| Build Tool | Gradle |
| Runtime | Java 21 |

---

## Features

- JWT-based authentication — signup, login
- User profiles with bio, profile picture, relationship status, and privacy settings
- Friend system — send, accept, reject, and remove connections
- Privacy controls — private profiles hidden from search and friend lists
- Friend-of-friend visibility — mutual friends can see each other even on private accounts
- Profile friendship status — know your connection state when viewing any profile
- Typeahead search by username
- Group chats — create groups, manage members and roles (OWNER / ADMIN / MEMBER)
- Direct and group messaging via a custom WebSocket JSON protocol
- Typing indicators and read receipts in real time
- Delete for me / delete for everyone
- Message editing
- Rich chat list — name, profile picture, and last message per conversation
- Cascade deletes — removing a user cleans up all related data automatically

---

## Prerequisites

- Java 21
- MySQL 8
- Gradle (wrapper included)

---

## Environment Variables

The app requires these environment variables at runtime. Set them in your IDE run configuration or export them in your shell before running.

| Variable | Description | Example |
|---|---|---|
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | `yourpassword` |
| `JWT_SECRET` | Secret key for signing JWT tokens (min 32 chars) | `ripple_super_secret_key_for_jwt` |
| `JWT_EXPIRATION` | Token expiry in milliseconds | `86400000` (24 hours) |

### IntelliJ Setup

Go to `Run → Edit Configurations → Modify Options → Environment Variables` and paste:

```
DB_USERNAME=root;DB_PASSWORD=yourpassword;JWT_SECRET=ripple_super_secret_key_for_jwt;JWT_EXPIRATION=86400000
```

---

## How to Run

1. Clone the repository
2. Create the MySQL database (Liquibase will create all tables automatically on first run):
   ```sql
   CREATE DATABASE IF NOT EXISTS messaging_app;
   ```
3. Set the environment variables (see above)
4. Run the application:
   ```bash
   ./gradlew bootRun
   ```
5. The server starts on `http://localhost:8081`

---

## Database Schema

Liquibase manages all migrations automatically. Tables are created in this order on startup:

| Table | Purpose |
|---|---|
| `user` | Credentials and privacy settings |
| `profile` | Display info — name, bio, profile picture |
| `friendship` | Connection state between two users |
| `groups` | Group metadata |
| `group_member` | Group membership and roles |
| `conversation` | Unified chat container for direct and group chats |
| `conversation_member` | Who belongs to each conversation |
| `message` | All messages across all conversations |
| `message_delete` | Per-user soft deletes |

---

## API Overview

All endpoints require `Authorization: Bearer <token>` except auth endpoints.

### Auth — `/api/auth`

| Method | Path | Description |
|---|---|---|
| POST | `/signup` | Register a new user |
| POST | `/login` | Login and get a token |

### Profile — `/api/profile`

| Method | Path | Description |
|---|---|---|
| POST | `/create` | Create your profile |
| PUT | `/update` | Update your profile |
| GET | `/{userId}` | Get a user's profile with friendship status |
| PATCH | `/privacy` | Toggle profile visibility |
| DELETE | `/remove` | Delete your account |

### Friendship — `/api/friendship`

| Method | Path | Description |
|---|---|---|
| POST | `/request/{receiverId}` | Send a friend request |
| PUT | `/accept/{senderId}` | Accept a friend request |
| DELETE | `/reject/{senderId}` | Reject a friend request |
| DELETE | `/remove/{friendId}` | Remove a friend |
| GET | `/friends` | Get your friend list |
| GET | `/{userId}/friends` | Get someone else's friend list |
| GET | `/pending` | Get incoming friend requests |
| GET | `/sent` | Get outgoing friend requests |

### Search — `/api/search`

| Method | Path | Description |
|---|---|---|
| GET | `/profile?username=` | Search users by username (typeahead) |

### Groups — `/api/groups`

| Method | Path | Description |
|---|---|---|
| POST | `/create` | Create a group |
| PUT | `/update` | Update group name and description |
| POST | `/{groupId}/add/{memberId}` | Add a member |
| DELETE | `/{groupId}/remove/{memberId}` | Remove a member |
| PUT | `/{groupId}/role/{memberId}` | Change a member's role |
| DELETE | `/leave/{groupId}` | Leave a group |
| GET | `/{groupId}` | Get group members |
| DELETE | `/delete/{groupId}` | Delete the group (owner only) |

### Messaging — `/api/message`

| Method | Path | Description |
|---|---|---|
| POST | `/create/conversation/{receiverId}` | Start or find a direct conversation |
| GET | `/get/conversation` | Get all chats with last message and name |
| GET | `/get/conversation/{conversationId}` | Get messages in a conversation |
| PATCH | `/edit/{messageId}` | Edit a message (sender only) |
| DELETE | `/delete/{messageId}?deleteType=` | Delete a message (`deleteForMe` or `deleteForEveryone`) |

---

## WebSocket Protocol

Connect to the WebSocket endpoint:

```
ws://localhost:8081/ws/chat?token=<your_jwt_token>
```

Authentication happens during the handshake via the token query parameter. If the token is invalid the connection is rejected with 401.

All messages follow this envelope structure:

```json
{
  "type": "EVENT_TYPE",
  "payload": { }
}
```

### Client → Server

**Send a direct message**
```json
{
  "type": "SEND_MESSAGE",
  "payload": {
    "conversationId": 1,
    "content": "Hello!"
  }
}
```

**Send a group message** (same structure, use the group's conversationId)
```json
{
  "type": "SEND_MESSAGE",
  "payload": {
    "conversationId": 5,
    "content": "Hello group!"
  }
}
```

**Typing indicator**
```json
{
  "type": "TYPING",
  "payload": {
    "conversationId": 1,
    "isTyping": true
  }
}
```

**Read receipt**
```json
{
  "type": "READ_RECEIPT",
  "payload": {
    "messageId": 12,
    "conversationId": 1
  }
}
```

### Server → Client

**Incoming message**
```json
{
  "type": "RECEIVE_MESSAGE",
  "payload": {
    "messageId": 12,
    "conversationId": 1,
    "senderId": 3,
    "content": "Hello!",
    "timestamp": "2026-07-18T23:11:32"
  }
}
```

**Message delivered confirmation (to sender)**
```json
{
  "type": "MESSAGE_DELIVERED",
  "payload": {
    "messageId": 12,
    "conversationId": 1,
    "deliveredAt": "2026-07-18T23:11:32"
  }
}
```

**Typing indicator (forwarded to other participant)**
```json
{
  "type": "TYPING",
  "payload": {
    "conversationId": 1,
    "senderId": 3,
    "isTyping": true
  }
}
```

**Read receipt (to original sender)**
```json
{
  "type": "READ_RECEIPT",
  "payload": {
    "messageId": 12,
    "readBy": 5,
    "readAt": "2026-07-18T23:11:32"
  }
}
```

---

## Group Roles

| Role | Can do |
|---|---|
| OWNER | Everything — including delete group and change any role |
| ADMIN | Add/remove members, change MEMBER roles, edit group |
| MEMBER | Send messages, leave group |

Role hierarchy — you can only act on members with a lower role than your own.

---

## Privacy Rules

- `isPrivate = true` — hidden from search results and all friend lists
- Private profile can still be viewed by existing friends
- Mutual friends (friends of friends who are also your friends) appear in friend lists even if they are private
- Friendship status is always returned when viewing any profile so the client knows which button to show

---

## Known Limitations / Future Work

- Read receipts are real-time only — not persisted, so offline users miss them
- `getChats` makes multiple DB calls per conversation — should be optimized with a JOIN query at scale
- No pagination on messages or friend lists yet
- No image upload — profile pictures are stored as URLs
- No push notifications for offline users
- No frontend — backend only

---

## Project Structure

```
src/main/kotlin/com/backend/ripple/
├── auth/           # Signup, login, JWT filter, security config
├── profile/        # Profile CRUD, privacy
├── friendship/     # Friend requests, friend list
├── search/         # Username search
├── group/          # Groups, members, roles
├── message/        # HTTP messaging endpoints, conversation management
├── websocket/      # WebSocket handler, handshake interceptor, session store
├── model/          # JPA entities
├── dto/            # Request and response DTOs
└── GlobalExceptionHandler.kt
```

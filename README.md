# WtOvr Bridge

> **Work in progress** — the project is under active development. APIs and data structures may change without notice.

Bridge between the WtOvr taekwondo competition software and any external system. WtOvr speaks a proprietary WT OVR protocol — this service translates it into a clean REST API backed by a local H2 database, manages the tournament bracket automatically as results come in, and broadcasts live match state to connected clients via WebSocket.

Tournament data is loaded via `POST /manager/uploadTournament` before the competition starts. The manager sends the full bracket as a JSON array of matches; the service persists athletes and matches and links them to the existing categories.

Built with Quarkus 3, Hibernate ORM Panache and Jakarta REST.

---

## Architecture

```
WtOvr app
    │
    ├── GET  /matches, /matches/{id}, /competitors, /participants ...
    ├── POST /matches/{id}/actions  ──► MatchEvent persisted
    │                               └─► ScoreboardBroadcaster ──► WebSocket clients (per mat)
    └── POST /matches/{id}/results  ──► TournamentService (advances winner to next match)

WtOvrResource  ──► MatchStateService  ──► H2 (local file)

WebSocket clients connect to ws://host/ws/mats/{mat} and receive live updates
for every action that occurs on that mat, regardless of which match is active.
```

---

## Data model

| Table          | PK                       | Description                                   |
|----------------|--------------------------|-----------------------------------------------|
| `categories`   | autoincrement            | Competition rules (rounds, times, thresholds) |
| `athletes`     | `ovrInternalId` (String) | Competitors (UUID generated if not provided)  |
| `matches`      | `matchNumber` (String)   | Bracket — QF/SF/F per mat                     |
| `match_events` | autoincrement            | Every scoring event during a match            |

Match numbers follow the pattern `<mat><sequence>`: `101`, `102`… for mat 1, `201`, `202`… for mat 2. The `nextMatchNumber` and `nextMatchColor` columns encode the bracket advancement path.

---

## Configuration

`src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:./wtovr
quarkus.hibernate-orm.schema-management.strategy=update
```

The database is stored as `wtovr.mv.db` in the working directory and persists between restarts. No external server required.

The H2 console is available at `http://localhost:8080/h2` in dev mode (JDBC URL: `jdbc:h2:./wtovr`, no credentials needed).

---

## API endpoints

### WT OVR protocol (consumed by WtOvr)

| Method | Path                         | Description                                 |
|--------|------------------------------|---------------------------------------------|
| GET    | `/matches`                   | All matches (filterable by `?filter[mat]=`) |
| GET    | `/matches/{id}`              | Single match                                |
| GET    | `/competitors/{id}`          | Competitor                                  |
| GET    | `/participants/{id}`         | Participant                                 |
| GET    | `/events/{id}`               | Event                                       |
| GET    | `/match-configurations/{id}` | Match configuration                         |
| POST   | `/matches/{id}/actions`      | Match action                                |
| POST   | `/matches/{id}/results`      | Match result                                |

### WebSocket (real-time scoreboard)

| Path             | Description                                                                                                                         |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `/ws/mats/{mat}` | Live match state for a mat. Receives a JSON message on every action from WtOvr (clock tick, score, penalty, round start/end, etc.) |

**Payload example:**

```json
{
  "matchNumber": "M001",
  "action": "SCORE_HOME_KICK",
  "round": 2,
  "roundTime": "01:23",
  "score": { "home": 5, "away": 3 },
  "penalties": { "home": 1, "away": 0 },
  "home": { "name": "KIM Cheol", "country": "KOR" },
  "away": { "name": "GARCIA Luis", "country": "ESP" }
}
```

Multiple clients can connect to the same mat simultaneously. When a match ends and a new one starts on the same mat, clients continue receiving updates without reconnecting.

### Manager

| Method | Path                           | Description                                     |
|--------|--------------------------------|-------------------------------------------------|
| GET    | `/manager/matches`             | All matches (indistinct)                        |
| GET    | `/manager/matches/{ring}`      | Matches available for a given mat               |
| GET    | `/manager/athletes`            | All athletes                                    |
| GET    | `/manager/categories`          | All categories                                  |
| POST   | `/manager/newCategory`         | Create a category                               |
| POST   | `/manager/uploadTournament`    | Load the full bracket (matches + athletes)      |
| POST   | `/manager/matches/{id}/winner` | Manually declare winner and advance bracket     |

`POST /manager/uploadTournament` expects a JSON array of `MatchInputDto`:

```json
[
  {
    "matchNumber": "101",
    "mat": 1,
    "phase": "QF",
    "categoryId": 1,
    "blueAthlete": { "scoreboardName": "...", "givenName": "...", "familyName": "...", "gender": "M" },
    "redAthlete":  { "scoreboardName": "...", "givenName": "...", "familyName": "...", "gender": "M" },
    "nextMatchNumber": "105",
    "nextMatchColor": "Blue"
  }
]
```

Athletes without `ovrInternalId` receive a generated UUID. `blueAthlete` / `redAthlete` can be `null` for bracket slots not yet assigned.

### Admin

| Method | Path           | Description       |
|--------|----------------|-------------------|
| DELETE | `/admin/reset` | Wipe the database |

---

## Running

```shell
./mvnw quarkus:dev
```

```shell
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

---

## License

Copyright © 2026 Alex Pazos Amoedo — [pazex04@gmail.com](mailto:pazex04@gmail.com)

This project is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.

You are free to use, study, modify and distribute this software under the terms of the AGPL-3.0. Any modified version deployed over a network must also make its source code available under the same license. See the [`LICENSE`](LICENSE) file for the full text.

---

## Contributing

Pull requests are welcome. Before opening one:

1. **Fork** the repository and create your branch from `master`.
2. Make sure the project builds cleanly: `./mvnw package -DskipTests`.
3. Keep changes focused — one concern per PR.
4. Follow the existing code style: no unnecessary comments, no unused abstractions.
5. If you're adding a new endpoint or changing the data model, update the README accordingly.

### Prerequisites

- Java 17+
- Maven 3.8+

### Local setup

```shell
# 1. Clone and enter the project
git clone https://github.com/Pazex04/TkStrike-Bridge.git
cd TkStrike-Bridge

# 2. Run in dev mode (database file and tables are created automatically on first start)
./mvnw quarkus:dev

# 3. Load tournament data
# POST http://localhost:8080/manager/uploadTournament  (JSON array of matches)
```

To wipe the database: `DELETE http://localhost:8080/admin/reset`

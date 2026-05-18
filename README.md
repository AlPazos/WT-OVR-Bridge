# TkStrike Bridge

Bridge between the [TkStrike](https://tkstrike.net) taekwondo competition software and any external system. TkStrike speaks a proprietary WT OVR protocol — this service translates it into a clean REST API backed by a MySQL database, and manages the tournament bracket automatically as results come in.

Built with Quarkus 3, Hibernate ORM Panache and Jakarta REST.

---

## Architecture

```
TkStrike app
    │
    ├── POST /{ring}/events-listener/new-match-configured
    ├── POST /{ring}/events-listener/new-match-event
    └── POST /{ring}/events-listener/match-result  ──► TournamentService (advances winner)
    
    GET  /matches, /matches/{id}, /competitors, /participants ...
    └── WtOvrResource  ──► MatchStateService  ──► MySQL
```

---

## Data model

| Table | PK | Description |
|---|---|---|
| `categories` | autoincrement | Competition rules (rounds, times, thresholds) |
| `athletes` | `ovrInternalId` (String) | Competitors |
| `matches` | `matchNumber` (String) | Bracket — QF/SF/F per mat |
| `match_events` | autoincrement | Every scoring event during a match |

Match numbers follow the pattern `<mat><sequence>`: `101`, `102`… for mat 1, `201`, `202`… for mat 2. The `nextMatchNumber` and `nextMatchColor` columns encode the bracket advancement path.

---

## Configuration

`src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/tkstrike
quarkus.datasource.username=prueba
quarkus.datasource.password=prueba
quarkus.datasource.db-version=5.5.5
quarkus.hibernate-orm.schema-management.strategy=update
```

---

## Initial data

Three CSV files under `src/main/resources/` define the tournament:

| File | Content |
|---|---|
| `categories.csv` | Category rules |
| `athletes.csv` | All athletes |
| `matches.csv` | Full bracket with athlete assignments for QF; SF/F left empty |

On startup, if the database is empty, the CSVs are loaded automatically.

---

## API endpoints

### WT OVR protocol (consumed by TkStrike)

| Method | Path | Description |
|---|---|---|
| GET | `/matches` | All matches (filterable by `?filter[mat]=`) |
| GET | `/matches/{id}` | Single match |
| GET | `/competitors/{id}` | Competitor |
| GET | `/participants/{id}` | Participant |
| GET | `/events/{id}` | Event |
| GET | `/match-configurations/{id}` | Match configuration |
| POST | `/matches/{id}/actions` | Match action |
| POST | `/matches/{id}/results` | Match result |

### Events listener (consumed by TkStrike)

| Method | Path | Description |
|---|---|---|
| GET | `/{ring}/events-listener/ping` | Health check |
| POST | `/{ring}/events-listener/new-match-configured` | Match configured |
| POST | `/{ring}/events-listener/new-match-event` | Scoring event (persisted) |
| POST | `/{ring}/events-listener/match-result` | Match result — triggers bracket advancement |

### Admin

| Method | Path | Description |
|---|---|---|
| DELETE | `/admin/reset` | Wipe the database |
| GET | `/admin/reload` | Wipe and reload from CSVs |

---

## Running

```shell
./mvnw quarkus:dev
```

```shell
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

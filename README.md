# TkStrike Bridge

> **Work in progress** — the project is under active development. APIs and data structures may change without notice.

Bridge between the TkStrike taekwondo competition software and any external system. TkStrike speaks a proprietary WT OVR protocol — this service translates it into a clean REST API backed by a MySQL database, and manages the tournament bracket automatically as results come in.

Built with Quarkus 3, Hibernate ORM Panache and Jakarta REST.

---

## Architecture

```
TkStrike app
    │
    ├── GET  /matches, /matches/{id}, /competitors, /participants ...
    ├── POST /matches/{id}/actions  ──► MatchEvent persisted
    └── POST /matches/{id}/results  ──► TournamentService (advances winner to next match)

WtOvrResource  ──► MatchStateService  ──► MySQL
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
- MySQL instance (any version; set `quarkus.datasource.db-version` in `application.properties` to match yours)

### Local setup

```shell
# 1. Clone and enter the project
git clone https://github.com/Pazex04/TkStrike-Bridge.git
cd TkStrike-Bridge

# 2. Create the database
mysql -u root -p -e "CREATE DATABASE tkstrike; CREATE USER 'prueba'@'localhost' IDENTIFIED BY 'prueba'; GRANT ALL ON tkstrike.* TO 'prueba'@'localhost';"

# 3. Run in dev mode (tables and data are created automatically on first start)
./mvnw quarkus:dev
```

To reset the database at any time: `GET http://localhost:8080/admin/reload`

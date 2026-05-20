# WtOvr Bridge

> **Work in progress** — the project is under active development. APIs and data structures may change without notice.

Bridge between the WtOvr taekwondo competition software and any external system. WtOvr speaks a proprietary WT OVR protocol — this service translates it into a clean REST API backed by a MySQL database, manages the tournament bracket automatically as results come in, and broadcasts live match state to connected clients via WebSocket.

The current data-loading mechanism (CSV files in `src/main/resources/`) is a **temporary example** to ease development. The planned flow is for the manager to upload a tournament PDF directly via a `/manager/tournament` endpoint; the service will parse the bracket and populate the database without any manual CSV generation. The `tools/gesbate2csv.py` script is an intermediate helper that converts Gesbate-exported PDFs into those CSVs until the endpoint is ready.

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

WtOvrResource  ──► MatchStateService  ──► MySQL

WebSocket clients connect to ws://host/ws/mats/{mat} and receive live updates
for every action that occurs on that mat, regardless of which match is active.
```

---

## Data model

| Table          | PK                       | Description                                   |
|----------------|--------------------------|-----------------------------------------------|
| `categories`   | autoincrement            | Competition rules (rounds, times, thresholds) |
| `athletes`     | `ovrInternalId` (String) | Competitors                                   |
| `matches`      | `matchNumber` (String)   | Bracket — QF/SF/F per mat                     |
| `match_events` | autoincrement            | Every scoring event during a match            |

Match numbers follow the pattern `<mat><sequence>`: `101`, `102`… for mat 1, `201`, `202`… for mat 2. The `nextMatchNumber` and `nextMatchColor` columns encode the bracket advancement path.

---

## Configuration

`src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/wtovr
quarkus.datasource.username=prueba
quarkus.datasource.password=prueba
quarkus.datasource.db-version=5.5.5
quarkus.hibernate-orm.schema-management.strategy=update
```

---

## Initial data (temporary, example only)

Three CSV files under `src/main/resources/` define the tournament:

| File             | Content                                                       |
|------------------|---------------------------------------------------------------|
| `categories.csv` | Category rules                                                |
| `athletes.csv`   | All athletes                                                  |
| `matches.csv`    | Full bracket with athlete assignments for QF; SF/F left empty |

On startup, if the database is empty, the CSVs are loaded automatically.

> **This is a placeholder mechanism.** Once `POST /manager/tournament` is implemented, the manager will upload the tournament PDF directly and the service will generate the full bracket — categories, athletes and matches — without any manual CSV step. The sample PDF `GALEGO SUB-21.pdf` and the `tools/gesbate2csv.py` script are kept as reference until then.

### gesbate2csv.py

Converts a Gesbate-exported tournament PDF into the three CSVs expected by the application. Run it pointing at one or more PDFs and copy the output to `src/main/resources/`:

```bash
python tools/gesbate2csv.py tournament.pdf
# copies athletes.csv, categories.csv and matches.csv to src/main/resources/
```

Match numbers are generated as `{mat}{seq:02d}` (e.g. `101`, `102` for mat 1, `201` for mat 2). WT weight codes (P1–P10) are resolved automatically from the weight label for Sub-21, Cadete and Senior age groups.

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

| Path              | Description                                                    |
|-------------------|----------------------------------------------------------------|
| `/ws/mats/{mat}`  | Live match state for a mat. Receives a JSON message on every action from WtOvr (clock tick, score, penalty, round start/end, etc.) |

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

| Method | Path                              | Description                                              |
|--------|-----------------------------------|----------------------------------------------------------|
| GET    | `/manager/matches`                | All matches (indistinct)                                 |
| GET    | `/manager/matches/{ring}`         | Matches available for a given mat                        |
| GET    | `/manager/athletes`               | All athletes                                             |
| GET    | `/manager/categories`             | All categories                                           |
| POST   | `/manager/newCategory`            | Create a category                                        |
| POST   | `/manager/matches`                | Create a match                                           |
| POST   | `/manager/matches/{id}/winner`    | Manually declare winner and advance bracket              |
| POST   | `/manager/tournament` *(planned)* | Upload a tournament PDF — generates the full bracket     |

### Admin

| Method | Path            | Description               |
|--------|-----------------|---------------------------|
| DELETE | `/admin/reset`  | Wipe the database         |
| GET    | `/admin/reload` | Wipe and reload from CSVs |

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

## Installation (database bootstrap)

This project expects a MySQL-compatible database available at jdbc:mysql://localhost:3306/wtovr.
You can create the database and user manually (see below) or use the provided helper script `install.sh`.

Note: `install.sh` tries to install MySQL/MariaDB using the host package manager and requires sudo/administrator privileges. It supports common Linux package managers (apt, dnf, yum, apk, pacman) and Homebrew on macOS. On Windows use Docker Desktop, WSL or follow the manual steps below.

Quick automatic bootstrap (Linux / macOS with sudo):

```bash
chmod +x install.sh
sudo ./install.sh
# This will attempt to install a server (if missing) and create the database/user:
#   database: wtovr
#   user:     prueba
#   password: prueba
```

If `install.sh` cannot install the server (no supported package manager, or missing privileges), run the SQL manually as a MySQL root user:

```sql
CREATE DATABASE IF NOT EXISTS `wtovr` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'prueba'@'localhost' IDENTIFIED BY 'prueba';
GRANT ALL PRIVILEGES ON `wtovr`.* TO 'prueba'@'localhost';
FLUSH PRIVILEGES;
```

Windows note
- On Windows it's recommended to use Docker Desktop or WSL. A PowerShell helper script is not included by default; you can run the SQL using the MySQL command-line client or MySQL Workbench after installing MySQL.

Recommended (portable) alternative: Docker Compose

If you prefer not to install the server on the host, use Docker Compose to run MySQL locally:

```yaml
version: "3.8"
services:
  db:
    image: mysql:8.0
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_DATABASE: wtovr
      MYSQL_USER: prueba
      MYSQL_PASSWORD: prueba
    ports:
      - "3306:3306"
    volumes:
      - db_data:/var/lib/mysql

volumes:
  db_data:
```

Start it with:

```bash
docker compose up -d
```

After the DB is ready, start the app as described in the "Running" section above.


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
- MySQL instance (any version; set `quarkus.datasource.db-version` in `application.properties` to match yours)

### Local setup

```shell
# 1. Clone and enter the project
git clone https://github.com/Pazex04/TkStrike-Bridge.git
cd TkStrike-Bridge

# 2. Create the database
mysql -u root -p -e "CREATE DATABASE wtovr; CREATE USER 'prueba'@'localhost' IDENTIFIED BY 'prueba'; GRANT ALL ON wtovr.* TO 'prueba'@'localhost';"

# 3. Run in dev mode (tables and data are created automatically on first start)
./mvnw quarkus:dev
```

To reset the database at any time: `GET http://localhost:8080/admin/reload`

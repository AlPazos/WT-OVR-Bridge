#!/usr/bin/env bash
set -euo pipefail

# install.sh: tries to install MySQL/MariaDB (when possible) and creates the DB/user if they don't exist.
# Designed for Unix-like environments (Debian/Ubuntu, Fedora/RHEL, CentOS, Alpine, Arch, macOS).
# DOES NOT guarantee installation in all environments (requires sudo/root and appropriate repositories).

DB_NAME="tkstrike"
DB_USER="prueba"
DB_PASS="prueba"

command_exists() { command -v "$1" >/dev/null 2>&1; }

info(){ echo "[INFO] $*"; }
warn(){ echo "[WARN] $*" >&2; }
err(){ echo "[ERROR] $*" >&2; exit 1; }

install_with_apt() {
  info "Using apt-get (Debian/Ubuntu)..."
  sudo DEBIAN_FRONTEND=noninteractive apt-get update
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y mysql-server || sudo DEBIAN_FRONTEND=noninteractive apt-get install -y mariadb-server
  sudo systemctl enable --now mysql 2>/dev/null || sudo systemctl enable --now mariadb 2>/dev/null || true
}

install_with_dnf() {
  info "Using dnf (Fedora/RHEL)..."
  sudo dnf install -y @mysql || sudo dnf install -y mariadb-server
  sudo systemctl enable --now mysqld 2>/dev/null || sudo systemctl enable --now mariadb 2>/dev/null || true
}

install_with_yum() {
  info "Using yum (CentOS/RHEL)..."
  sudo yum install -y mariadb-server
  sudo systemctl enable --now mariadb 2>/dev/null || true
}

install_with_apk() {
  info "Using apk (Alpine)..."
  sudo apk add --no-cache mariadb mariadb-client
  sudo rc-update add mariadb default
  sudo rc-service mariadb start
}

install_with_brew() {
  info "Using brew (macOS)..."
  brew install mysql || err "Could not install mysql with brew."
  brew services start mysql || true
}

install_with_pacman() {
  info "Using pacman (Arch/Manjaro)..."
  sudo pacman -Sy --noconfirm mariadb || err "Could not install mariadb with pacman."
  # Initialize database if needed
  if command_exists mariadb-install-db; then
    sudo mariadb-install-db --user=mysql --basedir=/usr --datadir=/var/lib/mysql || true
  elif command_exists mysql_install_db; then
    sudo mysql_install_db --user=mysql || true
  fi
  sudo systemctl enable --now mariadb || true
}

# Detect mysql client
if command_exists mysql; then
  info "mysql client found."
else
  info "mysql client not found — attempting to install according to the system."
  if command_exists apt-get; then
    install_with_apt
  elif command_exists dnf; then
    install_with_dnf
  elif command_exists yum; then
    install_with_yum
  elif command_exists apk; then
    install_with_apk
  elif command_exists pacman; then
    install_with_pacman
  elif command_exists brew; then
    install_with_brew
  else
    err "No supported package manager found (apt-get, dnf, yum, apk, pacman, brew). Install MySQL/MariaDB manually or use Docker."
  fi

  # Re-check
  if ! command_exists mysql; then
    warn "mysql client still not available after installation. The server may use socket authentication; we'll try 'sudo mysql' below."
  fi
fi

CREATE_SQL="CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\nCREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';\nGRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'localhost';\nFLUSH PRIVILEGES;"

info "Creating database and user (if they don't exist)..."

# Try to use sudo mysql first (common for socket auth setups), then fallback to mysql client if available, otherwise print instructions.
if sudo mysql -e "SELECT 1;" >/dev/null 2>&1; then
  sudo mysql -e "$CREATE_SQL" && info "Database and user configured successfully (via sudo mysql)." || warn "Failed to create DB/user with sudo mysql."
else
  # Intentar con cliente mysql en PATH
  if command_exists mysql; then
    if mysql -e "SELECT 1;" >/dev/null 2>&1; then
      mysql -e "$CREATE_SQL" && info "Database and user configured successfully (via mysql client)." || warn "Failed to create DB/user with mysql client."
    else
      warn "Cannot access mysql without credentials. Try executing the following command as root:"
      echo
      echo "  sudo mysql -e \"$CREATE_SQL\""
      echo
    fi
  else
    warn "No mysql client available to create the database automatically. Install the client or use Docker."
  fi
fi

info "Done. Check 'src/main/resources/application.properties' for credentials and start the app: ./mvnw quarkus:dev"


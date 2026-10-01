# A public IP, but no authorised networks: nothing can connect over it directly. The server uses
# the Cloud SQL socket factory, which authenticates as the server's service account and tunnels
# through the Cloud SQL Admin API.
resource "google_sql_database_instance" "postgres" {
  name             = "${local.name}-postgres"
  database_version = "POSTGRES_16"
  region           = var.region

  deletion_protection = var.database_deletion_protection

  settings {
    tier              = var.database_tier
    edition           = "ENTERPRISE"
    activation_policy = var.paused ? "NEVER" : "ALWAYS"

    ip_configuration {
      ipv4_enabled = true
      ssl_mode     = "ENCRYPTED_ONLY"
    }

    backup_configuration {
      enabled = true
    }
  }
}

resource "google_sql_database" "app" {
  name     = "backagain"
  instance = google_sql_database_instance.postgres.name
}

# Generated here, stored in Secret Manager, and never typed or seen by anyone.
resource "random_password" "postgres" {
  length  = 32
  special = false
}

resource "google_sql_user" "server" {
  name     = "backagain"
  instance = google_sql_database_instance.postgres.name
  password = random_password.postgres.result
}

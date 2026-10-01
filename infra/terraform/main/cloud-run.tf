resource "google_cloud_run_v2_service" "server" {
  name                = "${local.name}-server"
  location            = var.region
  ingress             = "INGRESS_TRAFFIC_ALL"
  deletion_protection = false

  # Pausing lives at the service level, where manual scaling to zero turns the service off without
  # deploying a revision that would have to start against a stopped database.
  scaling {
    scaling_mode          = var.paused ? "MANUAL" : "AUTOMATIC"
    manual_instance_count = var.paused ? 0 : null
  }

  template {
    service_account = google_service_account.server.email

    # A WebSocket is one long request, so it is cut at the request timeout; an hour is the most
    # Cloud Run allows. Clients reconnect and resynchronise when it happens.
    timeout = "3600s"

    # Every open socket counts against concurrency, so this is how many people one instance holds.
    max_instance_request_concurrency = 1000

    # The live hub is in memory: a second instance would have its own, and miss the first's events.
    scaling {
      min_instance_count = var.min_instances
      max_instance_count = 1
    }

    session_affinity = true

    containers {
      image = local.image

      ports {
        container_port = 8080
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "1Gi"
        }
        startup_cpu_boost = true
        # Billed only while serving. An open socket is a request, so during a talk the CPU is
        # never throttled; between talks an idle deck costs nothing.
        cpu_idle = true
      }

      env {
        name  = "POSTGRES_URL"
        value = "jdbc:postgresql:///${google_sql_database.app.name}?cloudSqlInstance=${google_sql_database_instance.postgres.connection_name}&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
      }
      env {
        name  = "POSTGRES_USER"
        value = google_sql_user.server.name
      }
      env {
        name  = "POSTGRES_MAX_POOL_SIZE"
        value = "5"
      }
      env {
        name  = "BACKAGAIN_GIT_SHA"
        value = var.git_sha
      }
      env {
        name  = "BACKAGAIN_BUILD_TIME"
        value = var.build_time
      }
      env {
        name = "POSTGRES_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.app["postgres-password"].secret_id
            version = "latest"
          }
        }
      }
      env {
        name = "BACKAGAIN_PRESENTER_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.app["presenter-password"].secret_id
            version = "latest"
          }
        }
      }

      startup_probe {
        http_get {
          path = "/healthz"
        }
        period_seconds    = 5
        failure_threshold = 24
      }

      liveness_probe {
        http_get {
          path = "/healthz"
        }
        period_seconds = 30
      }
    }
  }

  depends_on = [
    google_project_iam_member.server,
    google_secret_manager_secret_iam_member.server,
    google_secret_manager_secret_version.app,
    google_sql_user.server,
  ]
}

resource "google_cloud_run_v2_service_iam_member" "public" {
  location = google_cloud_run_v2_service.server.location
  name     = google_cloud_run_v2_service.server.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

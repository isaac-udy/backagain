# Two secrets, from two directions: the presenter password arrives from a GitHub Environment
# secret through the deploy job, and the database password is generated above. Either way, Cloud
# Run reads it from Secret Manager into an environment variable when a revision starts.
#
# Both values also pass through Terraform state, which is why the state bucket is private and
# versioned. Terraform 1.11's write-only arguments are the way to keep them out of it entirely.
locals {
  secrets = {
    "presenter-password" = var.presenter_password
    "postgres-password"  = random_password.postgres.result
  }
}

resource "google_secret_manager_secret" "app" {
  for_each = nonsensitive(toset(keys(local.secrets)))

  secret_id = "${local.name}-${each.key}"

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "app" {
  for_each = google_secret_manager_secret.app

  secret      = each.value.id
  secret_data = local.secrets[each.key]
}

resource "google_secret_manager_secret_iam_member" "server" {
  for_each = google_secret_manager_secret.app

  secret_id = each.value.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.server.email}"
}

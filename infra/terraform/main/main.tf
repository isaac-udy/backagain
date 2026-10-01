locals {
  name  = var.resource_prefix
  image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.resource_prefix}/server:${var.image_tag}"
}

# The identity the server runs as: allowed to reach Cloud SQL, read its two secrets, and write
# logs. Nothing else.
resource "google_service_account" "server" {
  account_id   = "${local.name}-server"
  display_name = "backagain server"
}

resource "google_project_iam_member" "server" {
  for_each = toset([
    "roles/cloudsql.client",
    "roles/logging.logWriter",
    "roles/monitoring.metricWriter",
  ])

  project = var.project_id
  role    = each.value
  member  = "serviceAccount:${google_service_account.server.email}"
}

# Cloud Run issues and renews the certificate itself. Creating the mapping needs the deployer to
# be a verified owner of the domain in Google Search Console, and the DNS records in the
# `domain_records` output to exist; see ../../README.md.
resource "google_cloud_run_domain_mapping" "server" {
  count = var.domain == "" ? 0 : 1

  location = var.region
  name     = var.domain

  metadata {
    namespace = var.project_id
  }

  spec {
    route_name = google_cloud_run_v2_service.server.name
  }
}

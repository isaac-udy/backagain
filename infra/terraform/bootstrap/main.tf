# Applied once, by a person with Owner on the project. Everything here is something the deploy
# pipeline needs before it can run, and nothing the pipeline should be able to change about itself.

resource "google_project_service" "enabled" {
  for_each = toset(var.activate_apis)

  service            = each.value
  disable_on_destroy = false
}

resource "google_storage_bucket" "terraform_state" {
  name     = var.state_bucket_name
  location = var.region

  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"

  versioning {
    enabled = true
  }

  lifecycle {
    prevent_destroy = true
  }

  depends_on = [google_project_service.enabled]
}

resource "google_artifact_registry_repository" "images" {
  location      = var.region
  repository_id = var.resource_prefix
  description   = "Server images, tagged with the commit that built them"
  format        = "DOCKER"

  depends_on = [google_project_service.enabled]
}

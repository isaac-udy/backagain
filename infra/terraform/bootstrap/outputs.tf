# Copy these into the GitHub Environment's variables after the first apply.

output "workload_identity_provider" {
  description = "GCP_WORKLOAD_IDENTITY_PROVIDER"
  value       = google_iam_workload_identity_pool_provider.github.name
}

output "deployer_service_account" {
  description = "GCP_DEPLOYER_SERVICE_ACCOUNT"
  value       = google_service_account.deployer.email
}

output "terraform_state_bucket" {
  description = "TF_STATE_BUCKET"
  value       = google_storage_bucket.terraform_state.name
}

output "image_repository" {
  description = "Where the deploy pushes server images"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.images.repository_id}"
}

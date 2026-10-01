output "service_url" {
  description = "The service's run.app address, which works whether or not the domain is mapped."
  value       = google_cloud_run_v2_service.server.uri
}

output "paused" {
  description = "Read back by each deploy, so a push doesn't wake a paused deck."
  value       = var.paused
}

output "release" {
  description = "What is deployed, so pausing and resuming can apply it again unchanged."
  value = {
    image_tag  = var.image_tag
    git_sha    = var.git_sha
    build_time = var.build_time
  }
}

output "domain_records" {
  description = "The DNS records the custom domain needs."
  value       = var.domain == "" ? [] : google_cloud_run_domain_mapping.server[0].status[0].resource_records
}

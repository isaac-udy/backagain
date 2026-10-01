variable "project_id" {
  description = "The GCP project to deploy into."
  type        = string
}

variable "region" {
  description = "Region for every regional resource. It must support Cloud Run domain mappings, which rules out australia-southeast1."
  type        = string
  default     = "asia-southeast1"
}

variable "resource_prefix" {
  description = "Prefix for resource names. The main stack is given the same value."
  type        = string
  default     = "backagain"
}

variable "state_bucket_name" {
  description = "Globally unique name for the bucket holding Terraform state for both stacks."
  type        = string
}

variable "github_repository" {
  description = "The `owner/name` of the only repository allowed to deploy."
  type        = string
  default     = "isaac-udy/backagain"
}

variable "github_environment" {
  description = "The GitHub Environment whose jobs may deploy. A job that does not declare it cannot get credentials."
  type        = string
  default     = "production"
}

variable "deployer_roles" {
  description = "Project roles for the identity GitHub Actions deploys as: exactly what the main stack manages."
  type        = list(string)
  default = [
    "roles/run.admin",
    "roles/artifactregistry.writer",
    "roles/cloudsql.admin",
    "roles/secretmanager.admin",
    "roles/iam.serviceAccountAdmin",
    "roles/iam.serviceAccountUser",
    "roles/resourcemanager.projectIamAdmin",
  ]
}

variable "activate_apis" {
  description = "APIs enabled here, so a deploy never needs the permission to enable one."
  type        = list(string)
  default = [
    "artifactregistry.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "iam.googleapis.com",
    "iamcredentials.googleapis.com",
    "logging.googleapis.com",
    "run.googleapis.com",
    "secretmanager.googleapis.com",
    "sqladmin.googleapis.com",
    "storage.googleapis.com",
    "sts.googleapis.com",
  ]
}

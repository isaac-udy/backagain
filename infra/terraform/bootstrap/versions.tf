terraform {
  required_version = ">= 1.9"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 6.0"
    }
  }

  # The state bucket is declared in this stack, so it is created by hand first and imported; see
  # the bootstrap steps in ../../README.md.
  backend "gcs" {}
}

provider "google" {
  project = var.project_id
  region  = var.region
}

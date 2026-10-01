terraform {
  required_version = ">= 1.9"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 6.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # The bucket the bootstrap stack created, passed with -backend-config.
  backend "gcs" {}
}

provider "google" {
  project = var.project_id
  region  = var.region
}

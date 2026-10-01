variable "project_id" {
  description = "The GCP project the bootstrap stack prepared."
  type        = string
}

variable "region" {
  description = "Must match the bootstrap stack's region."
  type        = string
  default     = "asia-southeast1"
}

variable "resource_prefix" {
  description = "Must match the bootstrap stack's prefix."
  type        = string
  default     = "backagain"
}

variable "image_tag" {
  description = "The server image to run, tagged with the commit that built it. Set by the deploy workflow."
  type        = string
}

variable "git_sha" {
  description = "The commit being deployed, shown on the deck's system slide."
  type        = string
  default     = "unknown"
}

variable "build_time" {
  description = "When the deployed build ran, shown on the deck's system slide."
  type        = string
  default     = "unknown"
}

variable "presenter_password" {
  description = "The password that turns a browser into the presenter. Comes from the PRESENTER_PASSWORD GitHub Environment secret."
  type        = string
  sensitive   = true
}

variable "min_instances" {
  description = "0 scales an idle deck to nothing, at the cost of a cold start for its first visitor; 1 keeps an instance warm. Comes from the MIN_INSTANCES GitHub Environment variable."
  type        = number
  default     = 0
}

variable "paused" {
  description = "Stops the server and the database, so between talks the deck costs only its storage. Set by the Pause or resume workflow; deploys keep whatever it last set."
  type        = bool
  default     = false
}

variable "domain" {
  description = "Custom domain mapped to the service, or empty for the run.app address alone. The deployer must be a verified owner of the domain. Comes from the DOMAIN GitHub Environment variable."
  type        = string
  default     = ""
}

variable "database_tier" {
  description = "Cloud SQL machine tier."
  type        = string
  default     = "db-f1-micro"
}

variable "database_deletion_protection" {
  description = "Set to false, and apply, before the database can be destroyed."
  type        = bool
  default     = true
}

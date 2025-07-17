variable "project_name" {
  description = "The name of the project"
  type        = string
  default     = "thoughtspace"
}

variable "aws_region" {
  description = "The AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "db_name" {
  description = "The name of the database"
  type        = string
  default     = "thoughtspacedb"
}

variable "db_user" {
  description = "Username for the RDS database"
  type        = string
}

variable "github_owner" {
  description = "GitHub repository owner"
  type        = string
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
}

variable "github_branch" {
  description = "GitHub repository branch"
  type        = string
  default     = "main"
}


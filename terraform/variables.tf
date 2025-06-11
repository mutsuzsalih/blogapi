variable "project_name" {
  description = "The name of the project"
  type        = string
  default     = "blogapi"
}
 
variable "aws_region" {
  description = "The AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "db_name" {
  description = "The name of the database"
  type        = string
  default     = "blogapidb"
}

variable "db_user" {
  description = "The username for the database"
  type        = string
  default     = "postgres"
} 
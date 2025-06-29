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
  description = "Username for the RDS database"
  type        = string
}

variable "db_password" {
  description = "Password for the RDS database"
  type        = string
  sensitive   = true
}

variable "ec2_key_name" {
  description = "Name of the EC2 key pair to use for the ECS instances"
  type        = string
}

variable "jwt_secret" {
  description = "Secret key for JWT generation and validation"
  type        = string
  sensitive   = true
} 
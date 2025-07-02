output "ecr_repository_url" {
  description = "ECR repository URL"
  value       = aws_ecr_repository.app.repository_url
}

output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.main.endpoint
}

output "db_password_secret_arn" {
  description = "ARN of the DB password secret in Secrets Manager"
  value       = aws_secretsmanager_secret.db_password.arn
  sensitive   = true
}

output "jwt_secret_arn" {
  description = "ARN of the JWT secret in Secrets Manager"
  value       = aws_secretsmanager_secret.jwt_secret.arn
  sensitive   = true
}

output "github_token_arn" {
  description = "ARN of the GitHub token in Secrets Manager"
  value       = aws_secretsmanager_secret.github_token.arn
  sensitive   = true
}

output "ssh_private_key_arn" {
  description = "ARN of the SSH private key secret in Secrets Manager"
  value       = aws_secretsmanager_secret.ssh_private_key.arn
  sensitive   = true
}

output "ec2_key_name" {
  description = "Name of the auto-generated EC2 key pair"
  value       = aws_key_pair.ec2_key.key_name
}

output "github_token" {
  description = "Generated GitHub token (only shown once)"
  value       = random_password.github_token.result
  sensitive   = true
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  description = "ECS service name"
  value       = aws_ecs_service.app.name
} 
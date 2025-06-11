output "load_balancer_dns" {
  description = "The DNS name of the application load balancer"
  value       = aws_lb.app_lb.dns_name
}
 
output "ecr_repository_url" {
  description = "The URL of the ECR repository"
  value       = aws_ecr_repository.app_ecr_repo.repository_url
} 
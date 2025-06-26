output "load_balancer_dns_name" {
  value       = aws_lb.app.dns_name
  description = "The DNS name of the application load balancer"
} 
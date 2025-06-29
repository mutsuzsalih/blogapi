# Define data sources for existing AWS resources
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*"]
  }
}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# RDS (Free Tier)
resource "aws_db_instance" "main" {
  allocated_storage      = 10
  engine                 = "postgres"
  engine_version         = "16"
  instance_class         = "db.t3.micro" # db.t3.micro is part of the free tier for RDS
  db_name                = var.db_name
  username               = var.db_user
  password               = var.db_password
  skip_final_snapshot    = true
  vpc_security_group_ids = [aws_security_group.db_sg.id]
  publicly_accessible    = false # Keep database private for security

  # Adding storage_type and max_allocated_storage for best practice with free tier
  # Though not strictly required, it's good to be explicit.
  storage_type          = "gp2"
  max_allocated_storage = 20 # Allows for some growth within free tier limits (20 GiB max for gp2)
}

resource "aws_security_group" "db_sg" {
  name        = "${var.project_name}-db-sg"
  description = "Allow inbound traffic from EC2 to the DB"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_sg.id] # Only allow traffic from ECS instances
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"] # Allow all outbound traffic from DB (e.g., for updates)
  }
}

# ECR repository to store the Docker image
resource "aws_ecr_repository" "app" {
  name = "${var.project_name}-repo"
  # Add lifecycle rule to prevent accidental deletion of images
  image_tag_mutability = "MUTABLE"
  image_scanning_configuration {
    scan_on_push = true
  }
}

# ECS Cluster (EC2 launch type)
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster"
}

# IAM role for ECS EC2 instances
resource "aws_iam_role" "ecs_instance_role" {
  name = "${var.project_name}-ecs-instance-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect    = "Allow",
      Principal = { Service = "ec2.amazonaws.com" },
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_instance_role_policy" {
  role       = aws_iam_role.ecs_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

# IAM Role for ECS Task Execution
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.project_name}-ecs-task-execution-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action = "sts:AssumeRole",
      Effect = "Allow",
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# IAM instance profile for ECS EC2
resource "aws_iam_instance_profile" "ecs_instance_profile" {
  name = "${var.project_name}-ecs-instance-profile"
  role = aws_iam_role.ecs_instance_role.name
}

# Security group for ECS EC2
resource "aws_security_group" "ecs_sg" {
  name        = "${var.project_name}-ecs-sg"
  description = "Allow SSH (22) and application traffic (8080) from anywhere"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # Allow SSH from anywhere (consider restricting this for production)
  }
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # Allow HTTP traffic from anywhere
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"] # Allow all outbound from ECS instances (for pulling images, communicating with DB, etc.)
  }

  lifecycle {
    prevent_destroy = true # avoid accidental deletion while instance attached
    ignore_changes  = [description] # description immutable; skip to avoid replace
  }
}

data "aws_ami" "ecs" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["amzn2-ami-ecs-hvm-*-x86_64-ebs"]
  }
}

# Launch Template for ECS EC2 instance (t2.micro, free tier)
resource "aws_launch_template" "ecs" {
  name_prefix   = "${var.project_name}-ecs-lt-"
  image_id      = data.aws_ami.ecs.id
  instance_type = "t2.micro" # t2.micro is part of the free tier
  key_name      = var.ec2_key_name
  network_interfaces {
    associate_public_ip_address = false # Keep EC2 instances private if possible, ALB handles public access
    security_groups             = [aws_security_group.ecs_sg.id]
  }
  iam_instance_profile {
    name = aws_iam_instance_profile.ecs_instance_profile.name
  }
  user_data = base64encode("#!/bin/bash\necho ECS_CLUSTER=${var.project_name}-cluster >> /etc/ecs/ecs.config\n")
}

# Auto Scaling Group (single t2.micro, free tier)
resource "aws_autoscaling_group" "ecs" {
  name                = "${var.project_name}-ecs-asg"
  max_size            = 1
  min_size            = 1
  desired_capacity    = 1
  vpc_zone_identifier = data.aws_subnets.default.ids
  launch_template {
    id      = aws_launch_template.ecs.id
    version = "$Latest"
  }
  tag {
    key                 = "Name"
    value               = "${var.project_name}-ecs-ec2"
    propagate_at_launch = true
  }
}

# ECS Task Definition (pulls image from ECR)
resource "aws_ecs_task_definition" "app" {
  family                   = "${var.project_name}-task"
  network_mode             = "bridge"
  requires_compatibilities = ["EC2"]
  cpu                      = "256" # Keep CPU low for t2.micro
  memory                   = "256" # LOWER MEMORY further; allows two tasks (2x256=512 MiB) on t2.micro during overlap.

  execution_role_arn = aws_iam_role.ecs_task_execution_role.arn
  container_definitions = jsonencode([
    {
      name         = "${var.project_name}-container"
      image        = "${aws_ecr_repository.app.repository_url}:latest"
      essential    = true
      portMappings = [{ containerPort = 8080, hostPort = 8080 }]
      environment = [
        {
          name  = "SPRING_DATASOURCE_URL"
          value = "jdbc:postgresql://${aws_db_instance.main.address}:${aws_db_instance.main.port}/${aws_db_instance.main.db_name}"
        },
        {
          name  = "SPRING_DATASOURCE_USERNAME"
          value = var.db_user
        },
        {
          name  = "SPRING_DATASOURCE_PASSWORD"
          value = var.db_password
        },
        {
          name  = "JWT_SECRET"
          value = var.jwt_secret
        },
        # Ensure this value is <= your 'memory' setting in the task definition
        {
          name  = "JAVA_OPTS"
          value = "-Xmx192m -Xms192m" # align JVM heap with lower task memory
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.app.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

resource "aws_cloudwatch_log_group" "app" {
  name = "/ecs/${var.project_name}"
  # Optionally add retention period to manage log costs
  retention_in_days = 7 # Keep logs for 7 days. Adjust as needed.
}

# ECS Service (single task)
resource "aws_ecs_service" "app" {
  name                               = "${var.project_name}-service"
  cluster                            = aws_ecs_cluster.main.id
  task_definition                    = aws_ecs_task_definition.app.arn
  desired_count                      = 1
  launch_type                        = "EC2"
  deployment_minimum_healthy_percent = 0 # Good for single instance to allow new task to spin up even if old one is not healthy
  deployment_maximum_percent         = 100
  # Allow some warm-up time before the ALB starts health-checking the new task
  health_check_grace_period_seconds = 60

  # Add deployment_controller for explicit ECS (not CodeDeploy)
  deployment_controller {
    type = "ECS"
  }

  # Ensure service waits for ASG to be ready
  depends_on = [aws_autoscaling_group.ecs]
} 
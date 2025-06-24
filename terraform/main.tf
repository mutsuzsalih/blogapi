# resource "aws_instance" "backend" {
#   ...
# }
#
# resource "aws_security_group" "ec2_sg" {
#   ...
# }

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
  allocated_storage    = 10
  engine               = "postgres"
  engine_version       = "16"
  instance_class       = "db.t3.micro"
  db_name              = var.db_name
  username             = var.db_user
  password             = random_password.db_password.result
  skip_final_snapshot  = true
  vpc_security_group_ids = [aws_security_group.db_sg.id]
  publicly_accessible  = false
}

resource "aws_security_group" "db_sg" {
  name        = "${var.project_name}-db-sg"
  description = "Allow inbound traffic from EC2 to the DB"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_sg.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "random_password" "db_password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# ECR repository to store the Docker image
resource "aws_ecr_repository" "app" {
  name = "${var.project_name}-repo"
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
      Effect = "Allow",
      Principal = { Service = "ec2.amazonaws.com" },
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_instance_role_policy" {
  role       = aws_iam_role.ecs_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

# IAM instance profile for ECS EC2
resource "aws_iam_instance_profile" "ecs_instance_profile" {
  name = "${var.project_name}-ecs-instance-profile"
  role = aws_iam_role.ecs_instance_role.name
}

# Security group for ECS EC2
resource "aws_security_group" "ecs_sg" {
  name        = "${var.project_name}-ecs-sg"
  description = "Allow HTTP/8080 and SSH"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Launch Template for ECS EC2 instance (t2.micro, free tier)
resource "aws_launch_template" "ecs" {
  name_prefix   = "${var.project_name}-ecs-lt-"
  image_id      = data.aws_ami.ecs.id
  instance_type = "t2.micro"
  key_name      = var.ec2_key_name
  iam_instance_profile {
    name = aws_iam_instance_profile.ecs_instance_profile.name
  }
  vpc_security_group_ids = [aws_security_group.ecs_sg.id]
  user_data = base64encode("#!/bin/bash\necho ECS_CLUSTER=${var.project_name}-cluster >> /etc/ecs/ecs.config\n")
}

data "aws_ami" "ecs" {
  most_recent = true
  owners      = ["591542846629"] # Amazon ECS AMIs
  filter {
    name   = "name"
    values = ["amzn2-ami-ecs-hvm-*-x86_64-ebs"]
  }
}

# Auto Scaling Group (single t2.micro, free tier)
resource "aws_autoscaling_group" "ecs" {
  name                      = "${var.project_name}-ecs-asg"
  max_size                  = 1
  min_size                  = 1
  desired_capacity          = 1
  vpc_zone_identifier       = data.aws_subnets.default.ids
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
  cpu                      = "256"
  memory                   = "512"
  container_definitions = jsonencode([
    {
      name      = "${var.project_name}-container"
      image     = "${aws_ecr_repository.app.repository_url}:latest"
      essential = true
      portMappings = [{ containerPort = 8080, hostPort = 8080 }]
    }
  ])
}

# ECS Service (single task)
resource "aws_ecs_service" "app" {
  name            = "${var.project_name}-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = 1
  launch_type     = "EC2"
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100
} 
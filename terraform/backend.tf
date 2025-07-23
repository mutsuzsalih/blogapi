# S3 bucket for Terraform state storage
resource "aws_s3_bucket" "terraform_state" {
  bucket = "thoughtspace-terraform-state-bucket-${random_id.state_bucket.hex}"
}

resource "random_id" "state_bucket" {
  byte_length = 4
}

resource "aws_s3_bucket_versioning" "terraform_state_versioning" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state_encryption" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "terraform_state_access_block" {
  bucket = aws_s3_bucket.terraform_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# DynamoDB table for Terraform state locking (Free Tier Compatible)
resource "aws_dynamodb_table" "terraform_locks" {
  name           = "thoughtspace-terraform-locks"
  billing_mode   = "PROVISIONED"
  read_capacity  = 1  # Free Tier: 25 RCU free
  write_capacity = 1  # Free Tier: 25 WCU free
  hash_key       = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Name = "Terraform State Lock Table"
    Project = var.project_name
  }
} 
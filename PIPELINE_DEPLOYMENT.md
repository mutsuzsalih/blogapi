# ThoughtSpace CI/CD Pipeline Deployment

Automated AWS deployment guide for ThoughtSpace blog platform with full CI/CD pipeline.

## Architecture

**Backend**: CodeBuild → ECR → ECS (Docker containers) + RDS (PostgreSQL)  
**Frontend**: CodeBuild → S3 + CloudFront (Static hosting)

## Prerequisites

- AWS CLI configured
- Terraform v1.0+
- GitHub repository
- GitHub Personal Access Token

## Quick Deploy

### 1. Configure Variables

Create `terraform/terraform.tfvars`:

```hcl
project_name    = "thoughtspace"
aws_region      = "us-east-1"
db_name         = "thoughtspacedb"
db_user         = "dbuser"
github_owner    = "YOUR_GITHUB_USERNAME"
github_repo     = "YOUR_REPO_NAME"
github_branch   = "main"
```

### 2. Deploy Infrastructure

```bash
cd terraform
terraform init
terraform apply
```

### 3. Configure GitHub

Add the generated token to GitHub repository webhooks:

```bash
terraform output github_token
```

### 4. Trigger Pipeline

Push any commit to trigger automatic deployment:

```bash
git push origin main
```

## Pipeline Flow

```
GitHub Push → CodePipeline
├── Backend: Build Docker → ECR → Deploy to ECS
└── Frontend: Build React → S3 → CloudFront Invalidation
```

## Access URLs

```bash
# Frontend URL
terraform output cloudfront_domain_name

# Backend API
# ECS Instance IP:8080/api/v1
```

## File Structure

```
├── buildspec.yml              # Backend build
├── frontend-buildspec.yml     # Frontend build
├── terraform/                 # Infrastructure as Code
└── PIPELINE_DEPLOYMENT.md     # This file
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Pipeline fails | Check CodePipeline console logs |
| Build errors | Verify CodeBuild environment variables |
| Frontend deploy | Check S3 permissions & CloudFront logs |

## Monitoring

- **CodePipeline**: Build status
- **CloudWatch**: Application logs
- **ECS**: Container health
- **RDS**: Database metrics

## Cost Optimization

Configured for AWS Free Tier:
- EC2 t2.micro (750h/month)
- RDS t3.micro (750h/month)  
- S3 storage (5GB)
- CloudFront (50GB transfer)

---

✅ **Ready**: Automatic deployment on every commit to both backend and frontend. 
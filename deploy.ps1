# deploy.ps1 - PowerShell script to deploy the application to AWS ECS

# Config
$REGION = "us-east-1"
$ACCOUNT_ID = "986527150026"
$REPO = "blogapi-repo"
$CLUSTER = "blogapi-cluster"
$SERVICE = "blogapi-service"
$ECR_URI = "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$REPO"

# ECR login
Write-Host "ECR login..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_URI

# Build image
Write-Host "Building image..."
docker build -t blogapi .

# Tag image
Write-Host "Tagging image..."
docker tag blogapi:latest "$ECR_URI:latest"

# Push image
Write-Host "Pushing to ECR..."
docker push "$ECR_URI:latest"

# ECS deploy
Write-Host "Updating ECS service..."
aws ecs update-service --cluster $CLUSTER --service $SERVICE --force-new-deployment --region $REGION

Write-Host "Done." 
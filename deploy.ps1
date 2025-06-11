# deploy.ps1 - PowerShell script to deploy the application to AWS ECS

# Variables
$aws_region = "us-east-1"
$aws_account_id = "986527150026"
$project_name = "blogapi"
$ecr_repo_name = "blogapi-repo"
$ecs_cluster_name = "blogapi-cluster"
$ecs_service_name = "blogapi-service"

# ECR repository URI
$ecr_repo_uri = "$aws_account_id.dkr.ecr.$aws_region.amazonaws.com/$ecr_repo_name"

# Step 1: Build Spring Boot application
Write-Host "Building Spring Boot application..."
./mvnw.cmd clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Maven build failed." -ForegroundColor Red
    exit 1
}

# Step 2: Build Docker image
Write-Host "Building Docker image..."
docker build -t $project_name .
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker build failed." -ForegroundColor Red
    exit 1
}

# Step 3: Log in to AWS ECR
Write-Host "Logging in to AWS ECR..."
aws ecr get-login-password --region $aws_region | docker login --username AWS --password-stdin $ecr_repo_uri
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker login failed. Check Docker Desktop is running and your system clock is correct." -ForegroundColor Red
    exit 1
}

# Step 4: Tag and Push Docker image
Write-Host "Tagging and pushing Docker image to ECR..."
docker tag "${project_name}:latest" "${ecr_repo_uri}:latest"
docker push "${ecr_repo_uri}:latest"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker push to ECR failed." -ForegroundColor Red
    exit 1
}

# Step 5: Force new deployment on ECS
Write-Host "Forcing new deployment on ECS service..."
aws ecs update-service --cluster $ecs_cluster_name --service $ecs_service_name --force-new-deployment --region $aws_region

Write-Host "Deployment successfully triggered. Changes should be live in a few minutes." -ForegroundColor Green 
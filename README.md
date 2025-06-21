# Blog API

A blog API developed with Spring Boot and PostgreSQL. This API includes user management, blog posts, tagging system, validation, logging, and role-based security.

## 🚀 Features

- User registration and authentication
- Create, edit, and delete blog posts
- Tagging system
- Role-based authorization (USER/ADMIN)
- JWT-based security
- Validation on all entities (User, Post, Tag)
- Spring Boot Actuator (health, metrics, etc.)
- Method-level security (@PreAuthorize)
- Centralized logging (console + logs/app.log)
- Unit & validation tests for all entities
- Swagger/OpenAPI documentation
- Docker support (with log volume)

## 🛠️ Technologies

- Java 21
- Spring Boot 3.x
- PostgreSQL
- Maven
- JWT
- Docker
- AWS (ECS, RDS, S3)
- Terraform
- Swagger/OpenAPI
- JUnit 5

## 📋 Prerequisites

- Java 21 or higher
- Maven
- PostgreSQL
- Docker (optional)
- AWS CLI & Terraform (for cloud deployment)

## 🚀 Getting Started

### Local Development Environment

1. Clone the project:
```bash
git clone https://github.com/mutsuzsalih/blogapi.git
cd blogapi
```

2. Set up PostgreSQL database and update connection details in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blogdb
spring.datasource.username=postgres
spring.datasource.password=2150
```

3. Build and run the project:
```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Running with Docker

```bash
docker-compose up --build
```
- Log files are stored on the host as `logs/app.log` when using Docker.

### Deploying to AWS (Terraform)

This project is configured for automated deployment to AWS.

1. Setup Infrastructure (First Time Only):
This command provisions all the necessary AWS resources (VPC, Database, Cluster, etc.). It can take 5-10 minutes.
```bash
cd terraform
terraform init
terraform apply
```

2. Deploy or Update the Application:
After making code changes, run the `deploy.ps1` script from the project root to deploy the new version.
```bash
# From project root
./deploy.ps1
```

3. Destroy Infrastructure:
To remove all created AWS resources and avoid costs, run the destroy command from the `terraform` directory.
```bash
cd terraform
terraform destroy
```

## 📖 API Usage

### User Operations

#### Register
```http
POST /api/users/register
Content-Type: application/json

{
    "username": "salih123",
    "email": "salihsansarci@example.com",
    "password": "password123"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "salih123",
    "password": "password123"
}
```

### Blog Post Operations

#### Create New Post
```http
POST /api/posts
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "title": "My First Blog Post",
    "content": "This is my first blog post...",
    "tagIds": [1, 2]
}
```

#### List Posts (ADMIN only)
```http
GET /api/posts
Authorization: Bearer {jwt_token}
```

## 🔍 API Documentation

Access the API documentation through Swagger UI:   http://localhost:8080/swagger-ui.html

## 🧪 Testing

To run all unit and validation tests:
```bash
./mvnw test
```
- Validation tests: All entity validation rules are covered in `src/test/java/com/blog/blogapi/model/*ValidationTest.java` files.
- Test rules: Setup, initialization, assertion, and independent tests are applied.

## 📦 Project Structure

src
├── main
│ ├── java
│ │ └── com
│ │ └── blogapi
│ │ ├── aspect
│ │ ├── config
│ │ ├── controller
│ │ ├── dto
│ │ ├── exception
│ │ ├── model
│ │ ├── repository
│ │ ├── security
│ │ └── service
│ └── resources
│ └── application.properties
└── test
└── java
└── com
└── blogapi
└── model
└── service

## 🔐 Security

- JWT-based authentication
- Role-based authorization
- Method-level security with `@PreAuthorize`
- Password encryption and secure data storage

## 📊 Monitoring & Logging
- Spring Boot Actuator: Endpoints like `/actuator/health`, `/actuator/metrics` are enabled.
- Log files: All logs are written to both console and `logs/app.log` file (visible on host when using Docker).

## 📞 Contact

Project Owner - [@mutsuzsalih](https://github.com/mutsuzsalih)

Project Link: [https://github.com/mutsuzsalih/blogapi](https://github.com/mutsuzsalih/blogapi)

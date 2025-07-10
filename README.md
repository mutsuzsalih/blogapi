# ThoughtSpace

A modern full-stack platform for sharing thoughts, ideas and stories. Built with Spring Boot backend and React frontend, featuring a beautiful user-friendly interface with advanced content management and admin capabilities.

## 🚀 Features

### Backend Features
- User registration and authentication
- Create, edit, and delete blog posts
- Tagging system with full CRUD operations
- Role-based authorization (USER/ADMIN)
- JWT-based security
- Search functionality
- Validation on all entities
- Spring Boot Actuator
- Method-level security
- Centralized logging
- Unit & validation tests
- Swagger/OpenAPI documentation
- Docker support

### Frontend Features
- Modern React application with responsive design
- Dark/Light mode toggle
- User authentication (login/register)
- Blog post creation and editing with rich text editor
- Advanced search functionality
- User profile management
- Admin panel for managing posts, tags, and users
- Real-time notifications
- Mobile-friendly interface
- Professional UI/UX design

## 🛠️ Technologies

### Backend
- Java 21
- Spring Boot 3.x
- PostgreSQL
- Maven
- JWT Authentication
- Docker
- Swagger/OpenAPI
- JUnit 5

### Frontend
- React 18
- Tailwind CSS
- React Router
- Axios
- React Quill (Rich Text Editor)
- Lucide React (Icons)
- React Toastify (Notifications)
- Dark Mode Support

## 📋 Prerequisites

- Java 21 or higher
- Node.js 16+ and npm
- Maven
- PostgreSQL
- Docker (optional)

## 🚀 Getting Started

### Backend Setup

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

3. Build and run the backend:
```bash
./mvnw clean install
./mvnw spring-boot:run
```

Backend will be available at: http://localhost:8080

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Create `.env` file in frontend directory:
```env
REACT_APP_API_URL=http://localhost:8080/api
```

4. Start the development server:
```bash
npm start
```

Frontend will be available at: http://localhost:3000

### Running with Docker

```bash
docker-compose up --build
```

## 📖 Frontend Features

### User Interface
- **Home Page**: Browse all blog posts with search functionality
- **Authentication**: Login and register pages with form validation
- **Post Creation**: Rich text editor for creating blog posts
- **Post Management**: Edit and delete your own posts
- **Profile Page**: View and manage your blog posts
- **Admin Panel**: Comprehensive admin dashboard for managing content
- **Dark Mode**: Toggle between light and dark themes

### Admin Panel Features
- **Posts Management**: View, edit, delete all blog posts
- **Tag Management**: Create, edit, delete tags
- **User Management**: View all registered users
- **Statistics**: Overview of total posts, tags, and users

## 📖 API Documentation

Access the API documentation through Swagger UI: http://localhost:8080/swagger-ui.html

## 🧪 Testing

### Backend Tests
```bash
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📦 Project Structure

```
blogapi/
├── src/                          # Backend source code
│   ├── main/java/com/blog/blogapi/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   └── dto/
│   └── test/
├── frontend/                     # Frontend React app
│   ├── src/
│   │   ├── components/
│   │   ├── contexts/
│   │   ├── services/
│   │   └── App.js
│   ├── public/
│   └── package.json
├── terraform/                    # AWS deployment
├── docker-compose.yml
└── pom.xml
```

## 🔐 Security

- JWT-based authentication with automatic token management
- Role-based authorization (User/Admin)
- Password encryption
- Protected routes on frontend
- CORS configuration for secure frontend-backend communication

## 🎨 Design Features

- Modern, clean interface design
- Responsive layout for all device sizes
- Dark/Light mode with smooth transitions
- Professional color scheme
- Intuitive navigation and user experience
- Loading states and error handling
- Toast notifications for user feedback

## 📞 Contact

Project Owner - [@mutsuzsalih](https://github.com/mutsuzsalih)

Project Link: [https://github.com/mutsuzsalih/blogapi](https://github.com/mutsuzsalih/blogapi)

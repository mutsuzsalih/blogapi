# Blog API Frontend

Modern, responsive React frontend for the Blog API application.

## 🚀 Features

- **Modern UI**: Clean and responsive design with Tailwind CSS
- **Authentication**: Secure login/register system with JWT tokens
- **Blog Management**: Create, read, update, and delete blog posts
- **Tag System**: Dynamic tag management for posts
- **Pagination**: Support for large datasets
- **Responsive Design**: Mobile and desktop compatible
- **Real-time Notifications**: Toast messages for user feedback

## 🛠️ Tech Stack

- **React 19**: Latest React version
- **React Router**: Page navigation and routing
- **Tailwind CSS**: Modern CSS framework
- **Axios**: HTTP requests
- **Lucide React**: Modern icons
- **React Toastify**: Notification system

## 📦 Setup

1. **Install dependencies:**
   ```bash
   cd frontend
   npm install --legacy-peer-deps
   ```

2. **Create environment file:**
   ```bash
   # Setup environment configuration:
   cp .env.example .env
   
   # Configure API endpoint:
   # REACT_APP_API_URL=http://localhost:8080/api
   ```

3. **Start development server:**
   ```bash
   npm start
   ```

## 🏗️ Project Structure

```
frontend/
├── public/                 # Static files
├── src/
│   ├── components/        # React components
│   │   ├── Header.js     # Navigation header
│   │   ├── Login.js      # Login page
│   │   ├── Register.js   # Register page
│   │   ├── Home.js       # Home page (blog list)
│   │   ├── CreatePost.js # Post creation
│   │   ├── PostDetail.js # Post detail page
│   │   └── Profile.js    # User profile
│   ├── contexts/         # React contexts
│   │   └── AuthContext.js # Authentication context
│   ├── services/         # API services
│   │   └── api.js        # Axios API configuration
│   ├── App.js            # Main app component
│   └── index.js          # React entry point
├── tailwind.config.js     # Tailwind CSS config
├── postcss.config.js      # PostCSS config
└── package.json           # Project dependencies
```

## 🔗 Routes

- `/` - Home page (blog posts list)
- `/login` - Login page
- `/register` - Register page
- `/create-post` - Create new post (auth required)
- `/post/:id` - Post detail page
- `/profile` - User profile (auth required)

## 🎨 Design Features

### Colors
- **Primary**: Blue tones (#3b82f6)
- **Gray**: Various gray tones for text and backgrounds
- **Success**: Green tones for success messages
- **Error**: Red tones for error messages

### Components
- **Card Layout**: Modern card design
- **Responsive Grid**: Automatic grid layout
- **Loading States**: Loading animations
- **Form Validation**: Real-time form validation
- **Toast Notifications**: User notifications

## 🔐 Authentication

Frontend uses JWT token-based authentication:

- Token stored in localStorage
- Automatic token management
- Protected routes with authentication guard
- Logout functionality with token cleanup

## 📱 Responsive Design

- **Mobile First**: Mobile-first approach
- **Breakpoints**: Tailwind CSS standard breakpoints
- **Touch Friendly**: Optimized for mobile devices

## 🚦 API Integration

Frontend uses the following API endpoints:

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Posts
- `GET /api/posts` - Blog posts list (with pagination)
- `GET /api/posts/{id}` - Single post detail
- `POST /api/posts` - Create new post
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post

### Tags
- `GET /api/tags` - Tags list
- `POST /api/tags` - Create new tag

### Users
- `GET /api/users/profile` - User profile
- `GET /api/posts/user/{userId}` - User's posts

## 🔧 Development

### Adding New Component
```jsx
import React from 'react';

const NewComponent = () => {
  return (
    <div className="bg-white shadow rounded-lg p-6">
      {/* Component content */}
    </div>
  );
};

export default NewComponent;
```

### Making API Calls
```jsx
import { postsAPI } from '../services/api';

const fetchPosts = async () => {
  try {
    const response = await postsAPI.getAllPosts();
    console.log(response.data);
  } catch (error) {
    console.error('Error:', error);
  }
};
```

## 📝 Usage Scenarios

1. **Guest User**:
   - View blog posts on home page
   - Read post details
   - Register or login

2. **Authenticated User**:
   - All guest features plus:
   - Create new blog posts
   - Edit/delete own posts
   - View profile page
   - Create tags

## 🎯 Future Features

- [ ] Post search functionality
- [ ] Comment system
- [ ] Social media sharing
- [ ] Dark mode
- [ ] Draft system
- [ ] Markdown support
- [ ] Image upload
- [ ] Real-time notifications

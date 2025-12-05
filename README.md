# README.md

This file provides essential context to help AI coding assistants quickly understand and work with the project.

## Project Overview

This is a Pet Management System (宠物管理系统) with a Spring Boot 3.2 backend and Vue 3 frontend. The system handles pet adoption, boarding, product sales, service appointments, and training course bookings.

## Build and Run Commands

### Backend (Spring Boot)
```bash
# Navigate to backend directory
cd springboot

# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# The server runs on port 12345
# API documentation: http://localhost:12345/doc.html
```

### Frontend (Vue 3)
```bash
# Navigate to frontend directory
cd vue3

# Install dependencies
npm install

# Development server (with hot reload)
npm run serve

# Production build
npm run build

# Lint files
npm run lint
```

## Architecture

### Backend Structure (`springboot/`)
- **Java 17** with **Spring Boot 3.2.0**
- **MyBatis-Plus 3.5.7** for ORM
- **MySQL 8.0** database
- **JWT** (java-jwt 4.4.0) for authentication
- **Knife4j** for API documentation (Swagger)
- **Hutool** utility library

Package structure under `src/main/java/org/example/springboot/`:
- `controller/` - REST API endpoints
- `service/` - Business logic layer
- `mapper/` - MyBatis-Plus data access interfaces
- `entity/` - JPA entities mapping to database tables
- `DTO/` - Data Transfer Objects for API requests/responses
- `config/` - Configuration classes (JWT interceptor, CORS, MyBatis-Plus, Security)
- `enumClass/` - Enumerations (OrderStatus, AdoptionStatus, etc.)
- `exception/` - Global exception handling
- `common/` - Common utilities (Result wrapper, ResultCode)

### Frontend Structure (`vue3/`)
- **Vue 3** with **Vue Router 4** and **Pinia** state management
- **Element Plus** UI component library
- **Axios** for HTTP requests (wrapped in `src/utils/request.js`)
- **ECharts** for data visualization
- **SCSS** for styling

Directory structure under `src/`:
- `views/frontend/` - Customer-facing pages (pet list, product shop, services, training)
- `views/backend/` - Admin management pages (dashboard, CRUD operations)
- `views/auth/` - Login and registration
- `layouts/` - FrontendLayout.vue and BackendLayout.vue
- `components/` - Reusable components (frontend/backend separated)
- `router/index.js` - Route definitions with dynamic route loading for admin menus
- `store/` - Pinia stores (user.js, app.js)
- `utils/request.js` - Axios wrapper with token handling and error messaging

### API Communication
- Frontend API base URL configured via `VUE_APP_BASE_API` environment variable (defaults to `/api`)
- JWT tokens stored in localStorage and sent via `token` header
- Standard response format: `{ code: "200", data: ..., msg: "..." }`

### Database
- MySQL database `pet_system`
- Schema file: `pet_system.sql` at project root
- Key tables: user, pet, adoption, boarding, product, order, service, service_appointment, training_course, training_appointment

## Key Patterns

### Authentication
- JWT-based authentication with `JwtInterceptor`
- Role-based access: USER (customers) vs admin roles (ADMIN, etc.)
- Admin users access `/back/*` routes; regular users access frontend routes

### Dynamic Menu System
- Admin menus stored in database and loaded at login
- Routes generated dynamically from menu configuration via `generateAsyncRoutes()` in router

### File Upload
- Files uploaded to `springboot/files/img/`
- Max file size: 10MB

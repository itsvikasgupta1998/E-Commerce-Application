# 🛒 E-Commerce-Application (Spring Boot + JWT + AWS EC2)

A production-style **E-Commerce Backend System** built using **Java, Spring Boot, MySQL, Docker, and AWS EC2 deployment**.

This project demonstrates a real-world backend system with authentication, role-based access, cart, orders, and cloud deployment (EC2 + Docker).

---

## ⚙️ Tech Stack

- Java 17+
- Spring Boot 3
- Spring Security + JWT (Auth0)
- Spring Data JPA / Hibernate
- MySQL
- Docker
- AWS EC2 (Deployment)
- Maven
- Swagger / OpenAPI

---

## ✨ Features

### 👨‍💼 Admin
- Manage Users
- Manage Categories
- Manage Products
- Manage Orders
- Manage Pricing & Discounts

### 👤 User
- User Registration & Login
- Browse Products & Categories
- Add / Remove Items from Cart
- Manage Address
- Place Orders
- Track Order Status

---

## 🔐 Security

- JWT-based Authentication (Auth0)
- Role-based Access Control (ADMIN / USER)
- Secure REST APIs using Spring Security
- Stateless session management

### Authorization Header
Send JWT token in request header:

Authorization: Bearer <your_jwt_token>

## 📡 API Documentation

Swagger UI: http://localhost:8080/swagger-ui/index.html

---

## 🧱 Architecture

- Layered Architecture (Controller → Service → Repository)
- DTO-based request/response design
- Clean separation of business logic
- Scalable monolithic backend design

---

## 🗄️ Database Entities

- Users
- Products
- Categories
- Orders
- Cart
- Address

---

## 🚀 Deployment (AWS EC2 + Docker)

This project is deployed on AWS EC2 instance using Docker & GitHub.

### 🖥️ Deployment Flow

- Code pushed to GitHub repository
- EC2 instance created (Ubuntu)
- Docker installed on EC2
- GitHub repo cloned on EC2
- Application started using Docker Compose
- Backend runs on cloud server

---

## ⚙️ EC2 Setup Commands
sudo apt update

sudo apt install docker.io -y 

sudo apt install docker-compose -y


## 📥 Clone Project on EC2
git clone https://github.com/itsvikasgupta1998/E-Commerce-Application.git

cd E-Commerce-Application

## 🐳 Run with Docker
docker compose up -d --build


## 🧹 Optional Cleanup
docker image prune -af


## 🌐 Live Deployment
Application is hosted on AWS EC2 instance:

http://3.110.49.172:8080


## 📦 API Modules
| Module   | Description                |
| -------- | -------------------------- |
| Auth     | Login & JWT authentication |
| User     | User management            |
| Product  | Product CRUD               |
| Category | Category management        |
| Cart     | Cart operations            |
| Order    | Order processing           |
| Address  | Address management         |


## 💳 Payment Integration

- Integrated **Stripe Payment Gateway**
- Secure checkout session creation
- Payment confirmation handling
- Webhook-based payment status update

## 📈 Future Improvements

- Microservices architecture migration
- Razorpay integration
- Redis caching
- Kafka-based order processing
- CI/CD pipeline (GitHub Actions + AWS automation)

## 👨‍💻 Author
Built by Vikas Gupta

GitHub: https://github.com/itsvikasgupta1998


## ⭐ Project Highlights
- Production-ready Spring Boot backend
- JWT authentication & role-based security
- Real-world e-commerce workflow
- Docker containerization
- AWS EC2 cloud deployment
- Scalable REST API design
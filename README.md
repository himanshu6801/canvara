# 🎨 Canvara Project

[![React](https://img.shields.io/badge/React-20232A?style=for-the-badge\&logo=react\&logoColor=61DAFB)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge\&logo=spring-boot\&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge\&logo=docker\&logoColor=white)](https://www.docker.com/)
[![MySQL](https://img.shields.io/badge/MySQL-00758F?style=for-the-badge\&logo=mysql\&logoColor=white)](https://www.mysql.com/)

Canvara is a full-stack web application featuring a **React frontend** (built with Vite) and a **Spring Boot backend** connected to a MySQL database.

---

## 🏗️ Project Structure

```text
canvara/
├── canvara-backend/      # Spring Boot REST API
├── canvara-frontend/     # React UI (Vite)
├── .env                  # Root environment configuration
└── docker-compose.yml    # Multi-container Docker orchestration
```

---

## ⚙️ Environment Configuration

### Frontend Environment Variables

Create a `.env` file inside the `canvara-frontend/` directory:

```env
VITE_API_BASE_URL=http://localhost:8080
```

---

## 🚀 Running the Application

You can run the application either using Docker or directly on your local machine.

### 📦 Option 1: Run with Docker

Build and start all services using Docker Compose:

```bash
docker compose up --build
```

This command will:

* Build the frontend and backend images
* Start all required containers
* Connect services using the Docker network defined in `docker-compose.yml`

---

### 💻 Option 2: Run Locally

Open two terminal windows and start each service separately.

#### 1. Start the Backend (Spring Boot)

```bash
cd canvara-backend
./mvnw spring-boot:run
```

Backend will be available at:

```text
http://localhost:8080
```

#### 2. Start the Frontend (React + Vite)

```bash
cd canvara-frontend
npm install
npm run dev
```

Frontend will typically be available at:

```text
http://localhost:5173
```

---

## 🛠️ Tech Stack

| Layer            | Technology             |
| ---------------- | ---------------------- |
| Frontend         | React, Vite            |
| Backend          | Spring Boot            |
| Database         | MySQL                  |
| Containerization | Docker, Docker Compose |

---

## 📋 Prerequisites

Before running the project, ensure you have:

* Java 17+ installed
* Node.js 18+ installed
* npm installed
* Docker & Docker Compose (for containerized setup)
* MySQL (for local development)

---

## 📄 License

This project is licensed under the MIT License.

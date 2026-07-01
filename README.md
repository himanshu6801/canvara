# 🎨 Canvara Project

[![React](https://img.shields.io/badge/React-20232A?style=for-the-badge\&logo=react\&logoColor=61DAFB)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge\&logo=spring-boot\&logoColor=white)](https://spring.io/projects/spring-boot)
[![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge\&logo=fastapi\&logoColor=white)](https://fastapi.tiangolo.com/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge\&logo=docker\&logoColor=white)](https://www.docker.com/)
[![MySQL](https://img.shields.io/badge/MySQL-00758F?style=for-the-badge\&logo=mysql\&logoColor=white)](https://www.mysql.com/)
[![OpenAI](https://img.shields.io/badge/OpenAI-412991?style=for-the-badge\&logo=openai\&logoColor=white)](https://openai.com/)

Canvara is a full-stack web application featuring a **React frontend** (built with Vite), a **Spring Boot backend** connected to a MySQL database, and a **FastAPI AI service** that powers a natural language artwork search chatbot.

---

## 🏗️ Project Structure

```text
canvara/
├── canvara-backend/      # Spring Boot REST API
├── canvara-frontend/     # React UI (Vite)
├── canvara-ai/           # FastAPI AI chatbot service (Python)
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

### AI Service Environment Variables

Create a `.env` file inside the `canvara-ai/` directory:

```env
OPENAI_API_KEY=your_openai_api_key
OPENAI_MODEL=gpt-4o-mini        # optional, defaults to gpt-4o-mini
BACKEND_BASE_URL=http://localhost:8080
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

#### 3. Start the AI Service (FastAPI)

```bash
cd canvara-ai
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

AI service will be available at:

```text
http://localhost:8000
```

API docs (Swagger UI):

```text
http://localhost:8000/docs
```

---

## 🤖 Canvara AI

`canvara-ai` is a FastAPI microservice that adds an AI-powered chatbot to the Canvara marketplace. Users can search for artworks using plain English, and the service translates those queries into structured filters.

### How it works

1. **User sends a natural language message** — e.g. *"Show me abstract paintings under $400"*
2. **LLM extracts filters** — OpenAI GPT parses the message into structured JSON (type, price range, size, keywords, sort order)
3. **Artwork fetch** — the service calls the Spring Boot backend with the extracted filters
4. **Conversational reply** — GPT generates a short, friendly summary of the results

### Endpoints

| Method | Path         | Description                              |
| ------ | ------------ | ---------------------------------------- |
| POST   | `/api/chat`  | Accept a natural language query, return artworks + reply |
| GET    | `/health`    | Health check                             |

### Example prompts

```
"Show me latest artwork of abstract type"
"Show me artwork less than $400 but in large size having a baby's face"
"Find me colorful landscape paintings sorted by price"
```

---

## 🛠️ Tech Stack

| Layer            | Technology             |
| ---------------- | ---------------------- |
| Frontend         | React, Vite            |
| Backend          | Spring Boot            |
| AI Service       | FastAPI, Python        |
| LLM              | OpenAI GPT (gpt-4o-mini) |
| Database         | MySQL                  |
| Containerization | Docker, Docker Compose |

---

## 📋 Prerequisites

Before running the project, ensure you have:

* Java 17+ installed
* Node.js 18+ installed
* npm installed
* Python 3.12+ installed
* Docker & Docker Compose (for containerized setup)
* MySQL (for local development)
* OpenAI API key (for the AI service)

---

## 📄 License

This project is licensed under the MIT License.

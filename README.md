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

## ☁️ Pushing Images to AWS ECR

Each service's Docker image can be built locally, tagged for Amazon ECR, and pushed up — the same flow [`setup-infra.sh`](setup-infra.sh) uses when provisioning the ECS Fargate deployment.

**This project's ECR registry:**

| | |
| --- | --- |
| AWS Account ID | `318731644726` |
| Region | `us-east-1` |
| Registry URI | `318731644726.dkr.ecr.us-east-1.amazonaws.com` |
| Repositories | `canvara-backend`, `canvara-ai`, `canvara-frontend` |

Prerequisites:

* AWS CLI installed and configured (`aws configure`) with credentials that have ECR push permissions
* Docker running locally
* The three ECR repositories already exist in the target account/region — `canvara-backend`, `canvara-ai`, `canvara-frontend` (one-time creation command below)

### 1. Set account/region variables

```bash
export AWS_REGION=us-east-1
export ACCOUNT_ID=318731644726   # or: $(aws sts get-caller-identity --query Account --output text)
export PROJECT=canvara
```

### 2. (One-time) create the ECR repositories

```bash
aws ecr create-repository --repository-name ${PROJECT}-backend  --region "$AWS_REGION"
aws ecr create-repository --repository-name ${PROJECT}-ai       --region "$AWS_REGION"
aws ecr create-repository --repository-name ${PROJECT}-frontend --region "$AWS_REGION"
```

### 3. Point local Docker at ECR

Logs the local Docker daemon into the account's ECR registry using a short-lived token from the AWS CLI (no long-lived Docker credentials stored anywhere):

```bash
aws ecr get-login-password --region "$AWS_REGION" | \
  docker login --username AWS --password-stdin "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
```

### 4. Build, tag, and push each image

All three ECS task definitions are pinned to **ARM64** (Graviton, for the Fargate cost/perf savings), so every build below targets `linux/arm64` explicitly via `--platform`. Don't drop that flag — building without it uses your local Docker daemon's default platform, and pushing an `amd64` image to an ARM64 task definition makes the ECS task fail to start (`CannotPullContainerError` / exec-format error), not a clean error at build or push time. This is a non-issue on an Apple Silicon Mac (native `arm64` host), but matters on any Intel/amd64 build machine or CI runner.

**Backend**

```bash
docker build --platform linux/arm64 -t ${PROJECT}-backend canvara-backend
docker tag ${PROJECT}-backend:latest "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT}-backend:latest"
docker push "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT}-backend:latest"
```

**AI service**

```bash
docker build --platform linux/arm64 -t ${PROJECT}-ai canvara-ai
docker tag ${PROJECT}-ai:latest "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT}-ai:latest"
docker push "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT}-ai:latest"
```

**Frontend**

`VITE_API_BASE_URL=""` is intentional — the deployed frontend calls the API on the same origin it's served from (the load balancer), so no separate API base URL is baked into the static build.

```bash
docker build --platform linux/arm64 -t ${PROJECT}-frontend canvara-frontend --build-arg VITE_API_BASE_URL=""
docker tag ${PROJECT}-frontend:latest "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT}-frontend:latest"
docker push "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT}-frontend:latest"
```

Resolved (no variables), for copy-paste reference:

```bash
docker build --platform linux/arm64 -t canvara-backend  .
docker tag canvara-backend:latest  318731644726.dkr.ecr.us-east-1.amazonaws.com/canvara-backend:latest
docker push 318731644726.dkr.ecr.us-east-1.amazonaws.com/canvara-backend:latest

docker build --platform linux/arm64 -t canvara-ai .
docker tag canvara-ai:latest       318731644726.dkr.ecr.us-east-1.amazonaws.com/canvara-ai:latest
docker push 318731644726.dkr.ecr.us-east-1.amazonaws.com/canvara-ai:latest

docker build --platform linux/arm64 -t canvara-frontend . --build-arg VITE_API_BASE_URL=""
docker tag canvara-frontend:latest 318731644726.dkr.ecr.us-east-1.amazonaws.com/canvara-frontend:latest
docker push 318731644726.dkr.ecr.us-east-1.amazonaws.com/canvara-frontend:latest
```

> Steps 3–4 (build → tag → push, all three services) run automatically as part of [`setup-infra.sh`](setup-infra.sh), which also stands up the VPC, ALB, RDS instance, and ECS services that pull these images.

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

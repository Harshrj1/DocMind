# DocMind Backend Service

A high-performance Spring Boot 4 & Java 21 REST API powering the DocMind AI document intelligence and Retrieval-Augmented Generation (RAG) platform.

---

## Features

- **Document Processing**: Ingests multi-format documents (PDF, DOCX, TXT) via Apache Tika and Spring AI PDF reader.
- **Vector Search**: Automatic text chunking, embedding generation using Google Gemini, and similarity search powered by PostgreSQL `pgvector`.
- **Hybrid AI Assistant**: Conversational Q&A grounded in document context with hybrid synthesis via Google Gemini Chat models.
- **Stateless JWT Security**: BCrypt password hashing, role-based access control (`ROLE_USER`, `ROLE_ADMIN`), and stateless JWT token authentication.
- **Production-Ready**: Multi-stage lightweight Docker image, configurable CORS, and OpenAPI (Swagger UI) documentation.

---

## Tech Stack

- **Java**: 21
- **Framework**: Spring Boot 4.1.0
- **AI Framework**: Spring AI 2.0.0 (Google GenAI Starter & pgvector Starter)
- **Database**: PostgreSQL 16+ with `pgvector`
- **Security**: Spring Security 6 + JJWT 0.13.0
- **API Docs**: Springdoc OpenAPI 3.1.0

---

## Directory Structure

```
backend/
├── .mvn/                         # Maven wrapper
├── database/
│   └── init.sql                  # PostgreSQL vector & uuid extensions
├── docs/                         # Architecture documentation & screenshots
├── src/
│   ├── main/java/com/substring/docmind/
│   │   ├── config/               # Security, CORS, JWT, ChatClient & OpenAPI configs
│   │   ├── controller/           # Auth, Chat, Document, and Health controllers
│   │   ├── dto/                  # Request / Response transfer objects
│   │   ├── entity/               # JPA entities (User, Conversation, Document, Message)
│   │   ├── exception/            # Global exception handlers
│   │   ├── repository/           # Spring Data JPA repositories
│   │   └── service/              # RAG, Document ingestion, Parsing, JWT, User services
│   └── main/resources/
│       ├── application.yaml      # Common application configuration
│       ├── application-dev.yml   # Development profile (local dev / Docker)
│       └── application-prod.yml  # Production profile (Render / Cloud deployment)
├── .dockerignore                 # Docker build exclusions
├── .env.example                  # Environment configuration template
├── .gitattributes                # Line ending normalizations
├── .gitignore                    # Git ignore rules for Java/Maven
├── Dockerfile                    # Multi-stage production container
├── docker-compose.yml            # Local PostgreSQL + pgvector setup
├── mvnw / mvnw.cmd               # Maven wrapper executables
├── pom.xml                       # Maven build configuration
└── README.md                     # Backend documentation
```

---

## Prerequisites

- **Java JDK 21+**: [Eclipse Temurin](https://adoptium.net/) or Oracle JDK
- **Docker**: For running PostgreSQL with pgvector locally
- **Google Gemini API Key**: [Get API Key](https://aistudio.google.com/)

---

## Getting Started (Local Development)

### 1. Start PostgreSQL with pgvector
From the `backend/` directory:
```bash
docker compose up -d
```
This launches a PostgreSQL 16 container on port `5434` with the `vector` extension pre-initialized via `init.sql`.

### 2. Configure Environment Variables
Copy the `.env.example` file:
```bash
cp .env.example .env
```
Set your environment variables (in `.env` or in your system environment):
```env
SPRING_PROFILES_ACTIVE=dev
PORT=8080
DB_HOST=localhost
DB_PORT=5434
DB_NAME=docmind
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password
GEMINI_API_KEY=your_gemini_api_key_here
JWT_SECRET=your_minimum_256_bit_secure_random_jwt_secret_key_here
APP_CORS_ALLOWED_ORIGINS=http://localhost:*,http://127.0.0.1:*,http://localhost:5173,http://localhost:5174,http://localhost:3000
```

### 3. Run the Application
Using the Maven wrapper:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

The backend server starts at `http://localhost:8080`.

- **Health Check**: `GET http://localhost:8080/api/v1/health`
- **Swagger Documentation**: `GET http://localhost:8080/swagger-ui.html`

---

## Environment Variables Reference

| Variable | Description | Required in Prod | Default |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active profile (`dev` or `prod`) | Yes (`prod`) | `dev` |
| `PORT` | Server HTTP port | Yes | `8080` |
| `DB_HOST` | PostgreSQL hostname | Yes | `localhost` |
| `DB_PORT` | PostgreSQL port | Yes | `5434` (dev) / `5432` (prod) |
| `DB_NAME` | PostgreSQL database name | Yes | `docmind` |
| `DB_USERNAME` | PostgreSQL username | Yes | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | Yes | (None) |
| `SPRING_DATASOURCE_URL` | Full JDBC URL (overrides DB_* if set) | Optional | (None) |
| `GEMINI_API_KEY` | Google Gemini API Key | Yes | (None) |
| `GEMINI_CHAT_MODEL` | Chat model name | No | `gemini-2.5-flash` |
| `GEMINI_EMBEDDING_MODEL` | Embedding model name | No | `gemini-embedding-001` |
| `JWT_SECRET` | 256-bit signing key for JWTs | Yes | (None) |
| `APP_CORS_ALLOWED_ORIGINS`| Comma-separated allowed frontend origins | Yes | `http://localhost:5173,http://localhost:3000` |

---

## Deployment to Render / Docker Platforms

### 1. Build and Test Container Locally
```bash
docker build -t docmind-backend .
docker run -p 8080:8080 --env-file .env docmind-backend
```

### 2. Render Deployment
1. Create a **New Web Service** connected to your backend repository.
2. Select **Docker** environment.
3. Configure Environment Variables:
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `PORT`: `8080`
   - `SPRING_DATASOURCE_URL`: Your managed PostgreSQL JDBC URL (e.g., `jdbc:postgresql://<host>:<port>/<dbname>?sslmode=require`)
   - `DB_USERNAME`: Database username
   - `DB_PASSWORD`: Database password
   - `GEMINI_API_KEY`: Your Gemini API key
   - `JWT_SECRET`: Secure JWT secret
   - `APP_CORS_ALLOWED_ORIGINS`: Deployed frontend URL (e.g., `https://docmind.vercel.app`)
4. Health Check Path: `/api/v1/health`
5. Deploy.

---

## API Endpoints

- **Authentication**:
  - `POST /api/v1/auth/register`: Register new user account
  - `POST /api/v1/auth/login`: Authenticate and obtain JWT bearer token
- **Document Management**:
  - `POST /api/v1/documents/upload`: Upload single document (PDF, TXT, DOCX)
  - `POST /api/v1/documents/upload-multiple`: Bulk document upload
  - `GET /api/v1/documents/user`: List current user's documents
  - `GET /api/v1/documents/{id}`: Get document details
  - `DELETE /api/v1/documents/{id}`: Delete document and embeddings
- **Conversational RAG**:
  - `POST /api/v1/chat/query`: Ask question grounded in uploaded documents
  - `POST /api/v1/chat/stream`: Stream answer via SSE
  - `POST /api/v1/chat/search/similarity`: Semantic similarity search
- **Monitoring**:
  - `GET /api/v1/health`: Service health status

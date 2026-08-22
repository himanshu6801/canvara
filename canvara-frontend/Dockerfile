# ── Stage 1: Build ────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first so npm install layer is cached
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build
# VITE_API_BASE_URL is injected at build time via --build-arg
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

COPY . .
RUN npm run build

# ── Stage 2: Serve static build ──────────────────────────────────
# No nginx here — routing (ALB in AWS, a standalone nginx service in
# local docker-compose) lives outside this image. This stage just
# serves the built files, with SPA fallback so React Router works.
FROM node:20-alpine AS runner

WORKDIR /app
RUN npm install -g serve

COPY --from=builder /app/dist ./dist

EXPOSE 80

# -s enables SPA mode: unknown paths fall back to index.html
CMD ["serve", "-s", "dist", "-l", "80"]

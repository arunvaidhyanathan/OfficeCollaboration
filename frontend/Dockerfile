# Build stage: produces the static Vite build output in /app/dist
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Export stage: this image's only job is to hold the built static files at /dist.
# In this POC's infra, a shared nginx container serves the frontend by mounting/copying
# this stage's /dist directory directly (bind mount in docker-compose, or a
# `COPY --from=officecollab-frontend-build /dist /usr/share/nginx/html` in the nginx
# image build) - this Dockerfile intentionally does not run its own web server.
FROM scratch AS export
COPY --from=build /app/dist /dist

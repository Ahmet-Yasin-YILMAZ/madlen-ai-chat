# 1) build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 2) serve
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html

# Vite build'i 4173 değil, nginx 80’de servis ediyor
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

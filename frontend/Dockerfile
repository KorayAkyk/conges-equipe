# Construction de l'interface, puis nginx pour la servir et relayer /api vers le backend.
FROM node:24-alpine AS construction
WORKDIR /src
COPY package.json package-lock.json .npmrc ./
RUN npm ci --ignore-scripts --no-audit
COPY . .
RUN npx ng build

FROM nginx:1.28-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=construction /src/dist/conges-front/browser /usr/share/nginx/html

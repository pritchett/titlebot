FROM node:18-alpine AS base

FROM base AS titlebot
RUN apk add --no-cache libc6-compat

WORKDIR /app
COPY package.json ./package.json
COPY package-lock.json ./package-lock.json
COPY next.config.js ./next.config.js
COPY next-env.d.ts ./next-env.d.ts
COPY postcss.config.js ./postcss.config.js
COPY tailwind.config.js ./tailwind.config.js
COPY tsconfig.json ./tsconfig.json
COPY app ./app

RUN npm ci
RUN npm run build

ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

USER nextjs

EXPOSE 3000

CMD ["npm", "run", "start"]

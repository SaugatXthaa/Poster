FROM node:20-bookworm-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig \
    fonts-inter \
    python3 \
    make \
    g++ \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY package*.json ./
RUN npm install --omit=dev

COPY . .

# Install the bundled PostersPlus fonts system-wide so librsvg (used by sharp)
# can resolve them by name when rendering rank numbers. Without this, the
# font-family selector in the configurator silently falls back to Inter.
RUN mkdir -p /usr/share/fonts/truetype/topx-posters && \
    cp public/fonts/*.ttf /usr/share/fonts/truetype/topx-posters/ 2>/dev/null || true && \
    fc-cache -f

ENV NODE_ENV=production
ENV DATA_DIR=/app/data
ENV MALLOC_ARENA_MAX=2

RUN mkdir -p /app/data

VOLUME ["/app/data"]
EXPOSE 3000

CMD ["npm", "start"]

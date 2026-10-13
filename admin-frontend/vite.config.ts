import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// The admin UI talks to the Kotlin bot's admin REST API (Ktor, see
// src/main/kotlin/web). In dev, Vite proxies /api/* to it so the browser
// stays same-origin and no CORS handling is needed. Override the target
// with API_PROXY_TARGET.
const apiTarget = process.env.API_PROXY_TARGET ?? "http://localhost:8081";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: apiTarget,
        changeOrigin: true,
      },
    },
  },
});

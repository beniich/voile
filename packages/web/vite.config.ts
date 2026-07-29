import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";
import { sentryVitePlugin } from "@sentry/vite-plugin";

const SENTRY_AUTH_TOKEN = process.env.SENTRY_AUTH_TOKEN;
const SENTRY_ORG = process.env.SENTRY_ORG || "voile";
const SENTRY_PROJECT = process.env.SENTRY_PROJECT || "voile-web";

export default defineConfig(({ mode }) => ({
  plugins: [
    react(),

    // Sentry source maps plugin (uniquement en build, pas en dev)
    ...(mode === "production" && SENTRY_AUTH_TOKEN
      ? [
          sentryVitePlugin({
            org: SENTRY_ORG,
            project: SENTRY_PROJECT,
            authToken: SENTRY_AUTH_TOKEN,
            // Supprime les source maps du bundle final (sécurité)
            hideSourceMaps: true,
            // Inclus le plugin React pour de meilleurs stack traces
            reactComponentAnnotation: {
              enabled: true,
            },
            // Supprime les sources du bundle final
            sourcemaps: {
              assets: ["./dist/**/*"],
              filesToDeleteAfterUpload: ["./dist/**/*.map"],
            },
            // Release tracking via Sentry CLI
            release: {
              name: process.env.SENTRY_RELEASE,
              create: true,
            },
          }),
        ]
      : []),

    VitePWA({
      registerType: "autoUpdate",
      manifest: {
        name: "Voile VPN",
        short_name: "Voile",
        description: "VPN transparent propulsé par Cloudflare WARP",
        lang: "fr",
        theme_color: "#26D9C4",
        background_color: "#0A0F1C",
        display: "standalone",
        icons: [
          { src: "/icons/icon-192.png", sizes: "192x192", type: "image/png" },
          { src: "/icons/icon-512.png", sizes: "512x512", type: "image/png" },
          {
            src: "/icons/icon-maskable-512.png",
            sizes: "512x512",
            type: "image/png",
            purpose: "maskable",
          },
        ],
      },
      workbox: {
        globPatterns: ["**/*.{js,css,html,ico,png,svg,woff2}"],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/1\.1\.1\.1\/cdn-cgi\/trace/,
            handler: "NetworkFirst",
            options: { cacheName: "voile-warp-trace" },
          },
          {
            urlPattern: /^https:\/\/.*\.supabase\.co\/rest\/v1\//,
            handler: "StaleWhileRevalidate",
            options: { cacheName: "voile-supabase" },
          },
        ],
      },
    }),
  ],
  build: {
    target: "es2022",
    sourcemap: mode === "production", // Génère les maps en prod
  },
  server: { port: 5173 },
}));

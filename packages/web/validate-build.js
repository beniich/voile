#!/usr/bin/env node
/**
 * validate-build.js — Vérifie que le build Cloudflare Pages est conforme.
 * Usage : node packages/web/validate-build.js
 */

import { existsSync, readFileSync } from "node:fs";
import { join, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const DIST = join(__dirname, "dist");

let passed = 0;
let failed = 0;

function check(label, condition, hint = "") {
  if (condition) {
    console.log(`  ✅ ${label}`);
    passed++;
  } else {
    console.error(`  ❌ ${label}${hint ? `\n     → ${hint}` : ""}`);
    failed++;
  }
}

console.log("\n🔍 Validation du build Cloudflare Pages — Voile PWA\n");

// 1. Dossier dist
check("dist/ existe", existsSync(DIST), "Lancer pnpm build d'abord.");

// 2. Fichiers obligatoires
const REQUIRED = ["index.html", "_headers", "_redirects"];
for (const file of REQUIRED) {
  check(
    `dist/${file} présent`,
    existsSync(join(DIST, file)),
    `Vérifier que packages/web/public/${file} existe.`
  );
}

// 3. Contenu _headers : CSP, HSTS, X-Frame-Options
if (existsSync(join(DIST, "_headers"))) {
  const headers = readFileSync(join(DIST, "_headers"), "utf-8");
  check(
    "_headers : Content-Security-Policy définie",
    headers.includes("Content-Security-Policy"),
    "Ajouter une ligne CSP dans public/_headers."
  );
  check(
    "_headers : HSTS défini",
    headers.includes("Strict-Transport-Security"),
    "Ajouter HSTS dans public/_headers."
  );
  check(
    "_headers : X-Frame-Options défini",
    headers.includes("X-Frame-Options"),
    "Ajouter X-Frame-Options: DENY dans public/_headers."
  );
  check(
    "_headers : cache immutable pour les assets",
    headers.includes("immutable"),
    "Ajouter Cache-Control: immutable pour /assets/*."
  );
}

// 4. Contenu _redirects : SPA fallback
if (existsSync(join(DIST, "_redirects"))) {
  const redirects = readFileSync(join(DIST, "_redirects"), "utf-8");
  check(
    "_redirects : SPA fallback (/* /index.html 200)",
    redirects.includes("/index.html") && redirects.includes("200"),
    "Le fichier public/_redirects doit contenir : /*  /index.html  200"
  );
}

// 5. index.html : pas de source map exposée
if (existsSync(join(DIST, "index.html"))) {
  const html = readFileSync(join(DIST, "index.html"), "utf-8");
  check(
    "index.html : pas de sourceMappingURL exposé",
    !html.includes("sourceMappingURL"),
    "Les source maps ne doivent pas être référencées dans le HTML en prod."
  );
}

// 6. wrangler.toml présent
check(
  "wrangler.toml présent",
  existsSync(join(__dirname, "wrangler.toml")),
  "Le fichier wrangler.toml est requis pour le déploiement."
);

// Résumé
console.log(`\n──────────────────────────────────────────`);
console.log(`  Résultats : ${passed} ✅  ${failed} ❌`);
if (failed === 0) {
  console.log("  🚀 Build valide — prêt pour le déploiement Cloudflare Pages !\n");
  process.exit(0);
} else {
  console.error("  ⛔ Problèmes détectés — corriger avant de déployer.\n");
  process.exit(1);
}

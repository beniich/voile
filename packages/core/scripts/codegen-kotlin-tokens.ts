#!/usr/bin/env node
/**
 * Génère Color.kt depuis les tokens TypeScript.
 * Usage : node --experimental-strip-types scripts/codegen-kotlin-tokens.ts
 */
import { writeFileSync, mkdirSync } from "node:fs";
import { join, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));

// Import direct — pas de transpilation nécessaire avec Node 25+
const { TOKENS } = await import("../src/tokens/index.ts");

const OUT_PATH = join(
  __dirname,
  "../../..",          // racine du projet
  "app/src/main/java/com/example/ui/theme/GeneratedTokens.kt"
);

/** Convertit #RRGGBB → 0xFF_RR_GG_BB pour Compose Color */
function hexToCompose(hex: string): string {
  const clean = hex.replace("#", "");
  return `Color(0xFF${clean.toUpperCase()})`;
}

/** camelCase → PascalCase */
function toPascal(s: string): string {
  return s.charAt(0).toUpperCase() + s.slice(1);
}

const solidEntries = Object.entries(TOKENS).filter(
  ([, v]) => typeof v === "string" && v.startsWith("#")
) as [string, string][];

const lines = solidEntries.map(
  ([key, hex]) => `val ${toPascal(key)} = ${hexToCompose(hex)}`
);

const kotlinCode = `// ⚠ FICHIER GÉNÉRÉ — Ne pas éditer manuellement.
// Source : packages/core/src/tokens/index.ts
// Régénérer avec : pnpm --filter @voile/core gen:tokens
package com.example.ui.theme

import androidx.compose.ui.graphics.Color

${lines.join("\n")}

// Variantes dim (alpha) — définies manuellement
val SecuredDim    = Color(0x2010B981)
val ConnectingDim = Color(0x20F5A623)
val ErrorDim      = Color(0x20EF4444)
`;

mkdirSync(dirname(OUT_PATH), { recursive: true });
writeFileSync(OUT_PATH, kotlinCode, "utf-8");
console.log(`✅ GeneratedTokens.kt → ${OUT_PATH}`);

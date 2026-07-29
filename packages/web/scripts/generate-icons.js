#!/usr/bin/env node
/**
 * Génère les icônes PWA depuis icon.svg.
 * Usage : node scripts/generate-icons.js
 *
 * Dépendance : pnpm add -D sharp
 */

import { readFileSync, mkdirSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import sharp from "sharp";

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, "..");
const SVG_PATH = resolve(ROOT, "public/icons/icon.svg");
const OUT_DIR = resolve(ROOT, "public/icons");

const SIZES = [
  { name: "icon-192.png", size: 192 },
  { name: "icon-512.png", size: 512 },
  { name: "icon-maskable-512.png", size: 512, maskable: true },
  { name: "icon-128.png", size: 128 },
  { name: "icon-48.png", size: 48 },
  { name: "icon-16.png", size: 16 },
  { name: "apple-touch-icon.png", size: 180 },
];

async function main() {
  const svg = readFileSync(SVG_PATH);
  mkdirSync(OUT_DIR, { recursive: true });

  for (const { name, size, maskable } of SIZES) {
    let pipeline;

    if (maskable) {
      // Pour maskable : padding interne de 40% pour la "safe zone"
      const innerSize = Math.floor(size * 0.6);
      const resized = await sharp(svg).resize(innerSize, innerSize).toBuffer();
      pipeline = sharp({
        create: {
          width: size,
          height: size,
          channels: 4,
          background: { r: 10, g: 15, b: 28, alpha: 1 },
        },
      })
        .composite([{ input: resized, gravity: "center" }])
        .png();
    } else {
      pipeline = sharp(svg).resize(size, size).png();
    }

    const out = resolve(OUT_DIR, name);
    await pipeline.toFile(out);
    console.log(`✅ ${name} (${size}x${size}${maskable ? " maskable" : ""})`);
  }

  // Favicon 32x32
  await sharp(svg)
    .resize(32, 32)
    .png()
    .toFile(resolve(ROOT, "public/favicon-32.png"));
  console.log("✅ favicon-32.png (32x32)");

  console.log("\n🎉 All icons generated!");
}

main().catch((e) => {
  console.error("❌", e);
  process.exit(1);
});

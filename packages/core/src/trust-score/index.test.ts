import { describe, it, expect, beforeEach, vi } from "vitest";
import { calculateTrustScore } from "./index.js";
import type { TrustScoreInputs } from "./index.js";

// Freeze Date.now pour que les timestamps soient déterministes
beforeEach(() => {
  vi.spyOn(Date, "now").mockReturnValue(1_700_000_000_000);
});

const optimal: TrustScoreInputs = {
  warpOn: true,
  latencyMs: 50,
  geolocExposed: false,
  webrtcMitigated: true,
};

describe("calculateTrustScore — scores", () => {
  it("retourne 100/A dans les conditions optimales", () => {
    const r = calculateTrustScore(optimal);
    expect(r.score).toBe(100);
    expect(r.grade).toBe("A");
    expect(r.issues).toHaveLength(0);
  });

  it("grade B entre 75 et 89", () => {
    const r = calculateTrustScore({ ...optimal, geolocExposed: true });
    expect(r.score).toBe(85); // 100 - 15
    expect(r.grade).toBe("B");
  });

  it("grade C entre 60 et 74", () => {
    // latence moyenne (-10) + géoloc (-15) = -25 → 75 → B… ajustons
    // latence haute (-20) + géoloc (-15) = -35 → 65 → C
    const r = calculateTrustScore({
      ...optimal,
      latencyMs: 400,
      geolocExposed: true,
    });
    expect(r.score).toBe(65);
    expect(r.grade).toBe("C");
  });

  it("grade D en dessous de 60", () => {
    const r = calculateTrustScore({
      warpOn: false,
      latencyMs: 400,
      geolocExposed: true,
      webrtcMitigated: false,
    });
    // DNS(-40) + WebRTC(-25) + géoloc(-15) + latence haute(-20) = -100 → 0
    expect(r.score).toBe(0);
    expect(r.grade).toBe("D");
  });

  it("score clampé à 0 (jamais négatif)", () => {
    const r = calculateTrustScore({
      warpOn: false,
      latencyMs: 1000,
      geolocExposed: true,
      webrtcMitigated: false,
    });
    expect(r.score).toBeGreaterThanOrEqual(0);
    expect(r.score).toBe(0);
  });
});

describe("calculateTrustScore — pénalités DNS", () => {
  it("DNS_LEAK → -40 si warpOn=false", () => {
    const r = calculateTrustScore({ ...optimal, warpOn: false });
    expect(r.issues.find((i) => i.code === "DNS_LEAK")).toBeDefined();
    expect(r.score).toBe(60); // 100 - 40
  });

  it("DNS_TEST_FAILED → -20 (prioritaire sur DNS_LEAK)", () => {
    const r = calculateTrustScore({
      ...optimal,
      warpOn: false,
      dnsTestFailed: true,
    });
    expect(r.issues.find((i) => i.code === "DNS_TEST_FAILED")).toBeDefined();
    expect(r.issues.find((i) => i.code === "DNS_LEAK")).toBeUndefined();
    expect(r.score).toBe(80); // 100 - 20
  });
});

describe("calculateTrustScore — pénalités WebRTC", () => {
  it("WEBRTC_EXPOSED → -25", () => {
    const r = calculateTrustScore({ ...optimal, webrtcMitigated: false });
    expect(r.issues.find((i) => i.code === "WEBRTC_EXPOSED")).toBeDefined();
    expect(r.score).toBe(75); // 100 - 25
  });
});

describe("calculateTrustScore — pénalités latence", () => {
  it("pas de pénalité si latence ≤ 150ms", () => {
    const r = calculateTrustScore({ ...optimal, latencyMs: 100 });
    expect(r.issues.filter((i) => i.code.includes("LATENCY"))).toHaveLength(0);
    expect(r.score).toBe(100);
  });

  it("MODERATE_LATENCY → -10 si 150 < latence ≤ 300ms", () => {
    const r = calculateTrustScore({ ...optimal, latencyMs: 200 });
    expect(r.issues.find((i) => i.code === "MODERATE_LATENCY")).toBeDefined();
    expect(r.score).toBe(90);
  });

  it("HIGH_LATENCY → -20 si latence > 300ms", () => {
    const r = calculateTrustScore({ ...optimal, latencyMs: 400 });
    expect(r.issues.find((i) => i.code === "HIGH_LATENCY")).toBeDefined();
    expect(r.score).toBe(80);
  });

  it("LATENCY_TEST_FAILED → -15, pas de HIGH/MODERATE", () => {
    const r = calculateTrustScore({
      ...optimal,
      latencyMs: 400,
      latencyTestFailed: true,
    });
    expect(r.issues.find((i) => i.code === "LATENCY_TEST_FAILED")).toBeDefined();
    expect(r.issues.find((i) => i.code === "HIGH_LATENCY")).toBeUndefined();
    expect(r.score).toBe(85); // 100 - 15
  });
});

describe("calculateTrustScore — métadonnées résultat", () => {
  it("inclut le timestamp", () => {
    const r = calculateTrustScore(optimal);
    expect(r.timestamp).toBe(1_700_000_000_000);
  });

  it("propagate le colo si fourni", () => {
    const r = calculateTrustScore({ ...optimal, colo: "CDG" });
    expect(r.colo).toBe("CDG");
  });

  it("colo null si non fourni", () => {
    const r = calculateTrustScore(optimal);
    expect(r.colo).toBeNull();
  });
});

// ── Types publics ─────────────────────────────────────────────────────────────

export interface TrustScoreInputs {
  /** WARP actif ? (DNS passant par Cloudflare) */
  warpOn: boolean;
  /** Latence mesurée vers Cloudflare en ms */
  latencyMs: number;
  /** L'API Geolocation est-elle accessible ? */
  geolocExposed: boolean;
  /** WebRTC est-il atténué (pas de fuite IP) ? */
  webrtcMitigated: boolean;
  /** Le test DNS a-t-il échoué (réseau inaccessible) ? */
  dnsTestFailed?: boolean;
  /** Le test de latence a-t-il échoué ? */
  latencyTestFailed?: boolean;
  /** Datacenter Cloudflare actuel (ex: "CDG") */
  colo?: string | null;
}

export type TrustGrade = "A" | "B" | "C" | "D";

export interface TrustIssue {
  code: string;
  severity: "high" | "medium" | "low";
  message: string;
}

export interface TrustScoreResult {
  score: number;         // 0–100
  grade: TrustGrade;
  issues: TrustIssue[];
  warpOn: boolean;
  colo: string | null;
  latencyMs: number;
  timestamp: number;
}

// ── Constantes de scoring ─────────────────────────────────────────────────────

const PENALTY = {
  DNS_LEAK:           40,
  DNS_TEST_FAILED:    20,
  WEBRTC_EXPOSED:     25,
  GEO_EXPOSED:        15,
  HIGH_LATENCY:       20,  // > 300ms
  MODERATE_LATENCY:   10,  // 150–300ms
  LATENCY_TEST_FAILED: 15,
} as const;

// ── Algorithme ────────────────────────────────────────────────────────────────

/**
 * Calcule le Trust Score à partir des mesures brutes.
 * Fonction pure — sans effet de bord, testable unitairement.
 */
export function calculateTrustScore(inputs: TrustScoreInputs): TrustScoreResult {
  let score = 100;
  const issues: TrustIssue[] = [];

  // ① DNS / WARP
  if (inputs.dnsTestFailed) {
    score -= PENALTY.DNS_TEST_FAILED;
    issues.push({
      code: "DNS_TEST_FAILED",
      severity: "medium",
      message: "Impossible de vérifier le routage DNS",
    });
  } else if (!inputs.warpOn) {
    score -= PENALTY.DNS_LEAK;
    issues.push({
      code: "DNS_LEAK",
      severity: "high",
      message: "Les requêtes DNS ne passent pas par Cloudflare WARP",
    });
  }

  // ② WebRTC
  if (!inputs.webrtcMitigated) {
    score -= PENALTY.WEBRTC_EXPOSED;
    issues.push({
      code: "WEBRTC_EXPOSED",
      severity: "high",
      message: "WebRTC peut exposer votre IP réelle aux sites visités",
    });
  }

  // ③ Géolocalisation
  if (inputs.geolocExposed) {
    score -= PENALTY.GEO_EXPOSED;
    issues.push({
      code: "GEO_EXPOSED",
      severity: "low",
      message: "L'API Geolocation est accessible pour les sites visités",
    });
  }

  // ④ Latence
  if (inputs.latencyTestFailed) {
    score -= PENALTY.LATENCY_TEST_FAILED;
    issues.push({
      code: "LATENCY_TEST_FAILED",
      severity: "medium",
      message: "Test de latence inaccessible — réseau potentiellement instable",
    });
  } else if (inputs.latencyMs > 300) {
    score -= PENALTY.HIGH_LATENCY;
    issues.push({
      code: "HIGH_LATENCY",
      severity: "medium",
      message: `Latence très élevée : ${Math.round(inputs.latencyMs)} ms`,
    });
  } else if (inputs.latencyMs > 150) {
    score -= PENALTY.MODERATE_LATENCY;
    issues.push({
      code: "MODERATE_LATENCY",
      severity: "low",
      message: `Latence moyenne : ${Math.round(inputs.latencyMs)} ms`,
    });
  }

  const finalScore = Math.max(0, score);

  return {
    score: finalScore,
    grade: scoreToGrade(finalScore),
    issues,
    warpOn: inputs.warpOn,
    colo: inputs.colo ?? null,
    latencyMs: inputs.latencyMs,
    timestamp: Date.now(),
  };
}

function scoreToGrade(score: number): TrustGrade {
  if (score >= 90) return "A";
  if (score >= 75) return "B";
  if (score >= 60) return "C";
  return "D";
}

// ── Helpers UI ────────────────────────────────────────────────────────────────

export function gradeLabel(grade: TrustGrade): string {
  return { A: "Excellent", B: "Bon", C: "Moyen", D: "Critique" }[grade];
}

export function gradeColor(grade: TrustGrade): string {
  return {
    A: "#10B981",
    B: "#3B82F6",
    C: "#F5A623",
    D: "#EF4444",
  }[grade];
}

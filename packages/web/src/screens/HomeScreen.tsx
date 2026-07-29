import React from "react";
import { Shield, ShieldCheck, Radar, AlertCircle } from "lucide-react";
import { TOKENS } from "@voile/core/tokens";
import { fr } from "@voile/core/i18n";
import { TelemetryCard } from "../components/TelemetryCard.js";
import { ServerCard } from "../components/ServerCard.js";
import type { TunnelState, WarpInfo, Server } from "../types.js";

interface HomeScreenProps {
  state: TunnelState;
  warpInfo: WarpInfo | null;
  realIp: string | null;
  server: Server;
  onToggleConnect: () => void;
  onGoServers: () => void;
  reducedMotion: boolean;
}

export function HomeScreen({
  state, warpInfo, realIp, server, onToggleConnect, onGoServers, reducedMotion,
}: HomeScreenProps) {
  const statusColor =
    state === "connected" ? TOKENS.secured :
    state === "connecting" ? TOKENS.connecting :
    state === "error" ? TOKENS.danger :
    TOKENS.textMuted;

  const statusLabel =
    state === "connected" ? fr.status.connected :
    state === "connecting" ? fr.status.connecting :
    state === "error" ? fr.status.error :
    fr.status.disconnected;

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 22, paddingBottom: 8 }}>
      {/* Status pill */}
      <div style={{ textAlign: "center", marginTop: 4 }}>
        <div
          role="status"
          aria-live="polite"
          aria-atomic="true"
          style={{
            display: "inline-flex",
            alignItems: "center",
            gap: 7,
            padding: "5px 12px",
            borderRadius: 999,
            background: TOKENS.surface,
            border: `1px solid ${TOKENS.borderSoft}`,
          }}
        >
          <span
            aria-hidden="true"
            style={{
              width: 6, height: 6, borderRadius: "50%",
              background: statusColor,
              boxShadow: state !== "disconnected" ? `0 0 8px ${statusColor}` : "none",
            }}
          />
          <span style={{ fontSize: 12.5, color: TOKENS.textSecondary, fontWeight: 500 }}>
            {statusLabel}
          </span>
        </div>
      </div>

      <ConnectButton state={state} onClick={onToggleConnect} server={server} reducedMotion={reducedMotion} />

      <ServerCard server={server} onClick={onGoServers} compact />

      {/* Avant/après IP */}
      {state === "connected" && realIp && warpInfo && (
        <div
          role="region"
          aria-label="Confirmation du changement d'adresse IP"
          style={{
            background: TOKENS.securedDim,
            border: `1px solid ${TOKENS.secured}`,
            borderRadius: 14, padding: "12px 14px",
            display: "flex", alignItems: "center", gap: 10,
          }}
        >
          <ShieldCheck size={16} color={TOKENS.secured} aria-hidden="true" />
          <div style={{ flex: 1, fontFamily: "'JetBrains Mono', monospace", fontSize: 12 }}>
            <div style={{ color: TOKENS.textMuted, fontSize: 10.5 }}>VOTRE IP EST MAINTENANT</div>
            <div style={{ color: TOKENS.textPrimary, fontWeight: 600 }}>{warpInfo.ip}</div>
            <div style={{ color: TOKENS.textMuted, fontSize: 10.5, marginTop: 4 }}>ANCIENNE IP</div>
            <div style={{ color: TOKENS.textSecondary, textDecoration: "line-through" }}>{realIp}</div>
          </div>
        </div>
      )}

      {/* Telemetry */}
      <section aria-labelledby="telemetry-heading">
        <h2 id="telemetry-heading" style={{
          position: "absolute", left: "-9999px", width: 1, height: 1, overflow: "hidden",
        }}>
          Statistiques de session
        </h2>
        <TelemetryCard
          icon="globe" label="Adresse IP publique"
          value={warpInfo?.ip ?? "—.—.—.—"}
        />
        <div style={{ display: "flex", gap: 10, marginTop: 10 }}>
          <TelemetryCard
            icon="download" label="Téléchargement"
            value={warpInfo?.download ?? "0.0"} unit="Mb/s"
          />
          <TelemetryCard
            icon="upload" label="Envoi"
            value={warpInfo?.upload ?? "0.0"} unit="Mb/s"
          />
        </div>
        <div style={{ marginTop: 10 }}>
          <TelemetryCard
            icon="clock" label="Durée de session"
            value={warpInfo?.duration ?? "00:00:00"}
          />
        </div>
      </section>

      <div
        role="note"
        style={{
          textAlign: "center", fontSize: 10.5,
          color: TOKENS.textMuted, letterSpacing: 0.8,
          textTransform: "uppercase", padding: "4px 0",
        }}
      >
        Simulation — aucune connexion réseau réelle
      </div>
    </div>
  );
}

// --- ConnectButton (extrait du legacy, adapté a11y) ---

interface ConnectButtonProps {
  state: TunnelState;
  onClick: () => void;
  server: Server;
  reducedMotion: boolean;
}

function ConnectButton({ state, onClick, server, reducedMotion }: ConnectButtonProps) {
  const color =
    state === "connected" ? TOKENS.secured :
    state === "connecting" ? TOKENS.connecting :
    state === "error" ? TOKENS.danger :
    TOKENS.idle;

  const ringCount = state === "connecting" ? 3 : state === "connected" ? 1 : 0;

  const visibleLabel =
    state === "connected" ? "Sécurisé" :
    state === "connecting" ? "Négociation" :
    state === "error" ? "Erreur" :
    "Se connecter";

  return (
    <div style={{ position: "relative", width: 220, height: 220, margin: "0 auto" }}>
      {Array.from({ length: ringCount }).map((_, i) => (
        <span
          key={i}
          aria-hidden="true"
          style={{
            position: "absolute", inset: 0, borderRadius: "50%",
            border: `1.5px solid ${color}`,
            animation: reducedMotion ? "none" :
              state === "connecting" ? `voile-sonar 1.8s ${i * 0.5}s infinite ease-out` :
              `voile-breathe 3.2s infinite ease-in-out`,
          }}
        />
      ))}

      <button
        onClick={onClick}
        aria-label={
          state === "connected" ? `Déconnecter du serveur ${server.city}` :
          state === "connecting" ? `Annuler la connexion à ${server.city}` :
          state === "error" ? `Réessayer la connexion à ${server.city}` :
          `Se connecter au serveur ${server.city}`
        }
        style={{
          position: "absolute", inset: 34, borderRadius: "50%",
          border: `1.5px solid ${color}`,
          background: `radial-gradient(circle at 35% 30%, ${TOKENS.surfaceElevated}, ${TOKENS.surface})`,
          boxShadow:
            state === "connected" ? `0 0 34px ${TOKENS.securedDim}, inset 0 0 24px ${TOKENS.securedDim}` :
            state === "connecting" ? `0 0 24px ${TOKENS.connectingDim}` :
            state === "error" ? `0 0 24px ${TOKENS.errorDim}` : "none",
          cursor: "pointer",
          display: "flex", flexDirection: "column",
          alignItems: "center", justifyContent: "center", gap: 8,
          transition: "box-shadow 0.4s ease, border-color 0.4s ease",
        }}
      >
        <span aria-hidden="true">
          {state === "connected" ? <ShieldCheck size={38} color={color} strokeWidth={1.6} /> :
           state === "connecting" ? <Radar size={34} color={color} strokeWidth={1.6} style={{ animation: reducedMotion ? "none" : "voile-spin 2.2s linear infinite" }} /> :
           state === "error" ? <AlertCircle size={34} color={color} strokeWidth={1.6} /> :
           <Shield size={36} color={color} strokeWidth={1.6} />}
        </span>
        <span style={{
          fontFamily: "'JetBrains Mono', monospace",
          fontSize: 11, letterSpacing: 1.5,
          textTransform: "uppercase", color: TOKENS.textMuted,
        }}>
          {visibleLabel}
        </span>
      </button>
    </div>
  );
}

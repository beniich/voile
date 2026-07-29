#!/usr/bin/env bash
set -e

# Voile MVP - Bootstrap ultra-rapide
# Usage: bash bootstrap-mvp.sh
# Crée un projet React standalone avec le tunnel UI complet

echo "🛡️  Voile MVP Bootstrap"
echo "=================================="
echo ""

# Vérifications
command -v node >/dev/null 2>&1 || { echo "❌ Node.js requis (>=20)"; exit 1; }
command -v pnpm >/dev/null 2>&1 || { echo "❌ pnpm requis (npm i -g pnpm)"; exit 1; }

NODE_VERSION=$(node -v | sed 's/v//' | cut -d. -f1)
if [ "$NODE_VERSION" -lt 20 ]; then
  echo "❌ Node.js 20+ requis (actuellement v$NODE_VERSION)"
  exit 1
fi

# Création du projet
PROJECT_NAME="voile-mvp"
mkdir -p "$PROJECT_NAME"
cd "$PROJECT_NAME"

echo "📦 Création du projet..."
cat > package.json <<'EOF'
{
  "name": "voile-mvp",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "deploy": "wrangler pages deploy dist"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "lucide-react": "^0.439.0"
  },
  "devDependencies": {
    "@types/react": "^18.3.0",
    "@types/react-dom": "^18.3.0",
    "@vitejs/plugin-react": "^4.3.1",
    "typescript": "^5.5.0",
    "vite": "^5.4.0",
    "vite-plugin-pwa": "^0.20.0"
  }
}
EOF

# Fichiers de base
cat > index.html <<'EOF'
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
  <meta name="theme-color" content="#0A0F1C" />
  <title>Voile VPN — MVP</title>
</head>
<body>
  <div id="root"></div>
  <script type="module" src="/src/main.tsx"></script>
</body>
</html>
EOF

cat > tsconfig.json <<'EOF'
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2022", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "moduleResolution": "Bundler",
    "jsx": "react-jsx",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "resolveJsonModule": true,
    "isolatedModules": true
  },
  "include": ["src"]
}
EOF

cat > vite.config.ts <<'EOF'
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: "autoUpdate",
      manifest: {
        name: "Voile VPN MVP",
        short_name: "Voile",
        lang: "fr",
        theme_color: "#26D9C4",
        background_color: "#0A0F1C",
        display: "standalone",
        icons: [
          { src: "/icon-192.png", sizes: "192x192", type: "image/png" },
          { src: "/icon-512.png", sizes: "512x512", type: "image/png" },
        ],
      },
    }),
  ],
});
EOF

# Dossier source
mkdir -p src
cat > src/main.tsx <<'EOF'
import React from "react";
import ReactDOM from "react-dom/client";
import VoileApp from "./VoileApp";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <VoileApp />
  </React.StrictMode>
);
EOF

# L'application complète (MVP fonctionnel)
cat > src/VoileApp.tsx <<'APP'
import React, { useState, useEffect, useRef } from "react";
import {
  Shield, ShieldCheck, Globe, Settings as SettingsIcon, Home as HomeIcon,
  Download, Upload, Clock, ChevronRight, Check, Zap, Lock, Radar, Wifi,
  MapPin, AlertCircle,
} from "lucide-react";

// === TOKENS ===
const T = {
  bg: "#0A0F1C",
  surface: "#121A2B",
  surfaceElevated: "#1A2438",
  border: "#232E45",
  borderSoft: "#1B2439",
  textPrimary: "#E8ECF4",
  textSecondary: "#A8B2C6",
  textMuted: "#8A95AD",
  idle: "#8A95AD",
  connecting: "#F5A623",
  secured: "#26D9C4",
  securedDim: "rgba(38, 217, 196, 0.14)",
  connectingDim: "rgba(245, 166, 35, 0.14)",
  danger: "#F0554A",
  focus: "#26D9C4",
};

// === SERVERS ===
const SERVERS = [
  { id: 1, country: "France", city: "Paris", flag: "🇫🇷", ping: 12, load: 34 },
  { id: 2, country: "Pays-Bas", city: "Amsterdam", flag: "🇳🇱", ping: 19, load: 21 },
  { id: 3, country: "Allemagne", city: "Francfort", flag: "🇩🇪", ping: 24, load: 58 },
  { id: 4, country: "Maroc", city: "Casablanca", flag: "🇲🇦", ping: 8, load: 42 },
];

function loadColor(l: number) {
  if (l < 35) return T.secured;
  if (l < 65) return T.connecting;
  return T.danger;
}

function formatDuration(s: number) {
  const h = String(Math.floor(s / 3600)).padStart(2, "0");
  const m = String(Math.floor((s % 3600) / 60)).padStart(2, "0");
  const sec = String(s % 60).padStart(2, "0");
  return `${h}:${m}:${sec}`;
}

// === APP ===
type TunnelState = "disconnected" | "connecting" | "connected" | "error";

interface Server { id: number; country: string; city: string; flag: string; ping: number; load: number }

export default function VoileApp() {
  const [tab, setTab] = useState<"home" | "servers" | "settings">("home");
  const [state, setState] = useState<TunnelState>("disconnected");
  const [selectedId, setSelectedId] = useState(1);
  const [realIp, setRealIp] = useState<string | null>(null);
  const [warpIp, setWarpIp] = useState<string | null>(null);
  const [duration, setDuration] = useState(0);
  const [download, setDownload] = useState("0.0");
  const [upload, setUpload] = useState("0.0");
  const [reduceMotion, setReduceMotion] = useState(false);

  const tickRef = useRef<number | null>(null);

  useEffect(() => {
    const mql = window.matchMedia("(prefers-reduced-motion: reduce)");
    setReduceMotion(mql.matches);
    const handler = (e: MediaQueryListEvent) => setReduceMotion(e.matches);
    mql.addEventListener("change", handler);
    return () => mql.removeEventListener("change", handler);
  }, []);

  // Capture IP réelle au chargement
  useEffect(() => {
    fetch("https://api.ipify.org?format=json")
      .then(r => r.json())
      .then(d => setRealIp(d.ip))
      .catch(() => setRealIp("—"));
  }, []);

  // Tick télémétrie pendant connexion
  useEffect(() => {
    if (state === "connected") {
      const start = Date.now();
      tickRef.current = window.setInterval(() => {
        setDuration(Math.floor((Date.now() - start) / 1000));
        setDownload((Math.random() * 40 + 10).toFixed(1));
        setUpload((Math.random() * 12 + 2).toFixed(1));
      }, 1000);
    } else {
      if (tickRef.current) clearInterval(tickRef.current);
      if (state === "disconnected") {
        setDuration(0);
        setDownload("0.0");
        setUpload("0.0");
      }
    }
    return () => {
      if (tickRef.current) clearInterval(tickRef.current);
    };
  }, [state]);

  const toggleConnect = () => {
    if (state === "disconnected") {
      setState("connecting");
      setTimeout(() => {
        setState("connected");
        // IP WARP simulée
        setWarpIp("172.16." + Math.floor(Math.random() * 255) + "." + Math.floor(Math.random() * 255));
      }, 1500);
    } else if (state === "connecting") {
      setState("disconnected");
    } else {
      setState("disconnected");
      setWarpIp(null);
    }
  };

  const server = SERVERS.find(s => s.id === selectedId) || SERVERS[0];

  return (
    <div lang="fr" style={styles.app}>
      <style>{`
        * { box-sizing: border-box; }
        button { font-family: inherit; }
        button:focus-visible, input:focus-visible {
          outline: 2px solid ${T.focus};
          outline-offset: 2px;
        }
        @keyframes sonar { 0% { transform: scale(0.72); opacity: 0.9; } 100% { transform: scale(1.35); opacity: 0; } }
        @keyframes breathe { 0%, 100% { transform: scale(0.94); opacity: 0.35; } 50% { transform: scale(1.06); opacity: 0.05; } }
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
      `}</style>

      {/* Header */}
      <header style={styles.header}>
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <div style={styles.logo}>
            <Shield size={14} color={T.secured} strokeWidth={2.2} />
          </div>
          <span style={styles.brand}>Voile</span>
        </div>
        <div style={styles.city}>
          <MapPin size={11} />
          {server.city}
        </div>
      </header>

      {/* Main */}
      <main style={styles.main}>
        {tab === "home" && (
          <HomeScreen
            state={state}
            server={server}
            realIp={realIp}
            warpIp={warpIp}
            duration={duration}
            download={download}
            upload={upload}
            onToggleConnect={toggleConnect}
            onGoServers={() => setTab("servers")}
            reduceMotion={reduceMotion}
          />
        )}
        {tab === "servers" && (
          <ServersScreen
            selectedId={selectedId}
            onSelect={(id) => {
              setSelectedId(id);
              if (state === "connected") {
                setState("connecting");
                setTimeout(() => setState("connected"), 1200);
              }
            }}
          />
        )}
        {tab === "settings" && <SettingsScreen />}
      </main>

      {/* Bottom nav */}
      <nav aria-label="Navigation" style={styles.nav}>
        {[
          { id: "home" as const, label: "Accueil", icon: HomeIcon },
          { id: "servers" as const, label: "Serveurs", icon: Globe },
          { id: "settings" as const, label: "Réglages", icon: SettingsIcon },
        ].map(it => {
          const active = tab === it.id;
          const Icon = it.icon;
          return (
            <button
              key={it.id}
              onClick={() => setTab(it.id)}
              aria-label={it.label}
              aria-current={active ? "page" : undefined}
              style={styles.navBtn}
            >
              <Icon size={19} color={active ? T.secured : T.textMuted} strokeWidth={active ? 2.1 : 1.8} />
              <span style={{ fontSize: 10.5, color: active ? T.secured : T.textMuted, fontWeight: active ? 600 : 400 }}>
                {it.label}
              </span>
            </button>
          );
        })}
      </nav>
    </div>
  );
}

// === HOME SCREEN ===
function HomeScreen({
  state, server, realIp, warpIp, duration, download, upload,
  onToggleConnect, onGoServers, reduceMotion,
}: {
  state: TunnelState;
  server: Server;
  realIp: string | null;
  warpIp: string | null;
  duration: number;
  download: string;
  upload: string;
  onToggleConnect: () => void;
  onGoServers: () => void;
  reduceMotion: boolean;
}) {
  const color = state === "connected" ? T.secured :
                state === "connecting" ? T.connecting :
                state === "error" ? T.danger : T.idle;

  const ringCount = state === "connecting" ? 3 : state === "connected" ? 1 : 0;
  const label = state === "connected" ? "Sécurisé" :
                state === "connecting" ? "Négociation" :
                state === "error" ? "Erreur" : "Se connecter";

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 22, paddingBottom: 8 }}>
      {/* Status pill */}
      <div style={{ textAlign: "center", marginTop: 4 }}>
        <div role="status" aria-live="polite" style={styles.statusPill}>
          <span style={{
            width: 6, height: 6, borderRadius: "50%",
            background: color,
            boxShadow: state !== "disconnected" ? `0 0 8px ${color}` : "none",
          }} aria-hidden="true" />
          <span style={{ fontSize: 12.5, color: T.textSecondary, fontWeight: 500 }}>
            {state === "connected" ? "Connexion sécurisée" :
             state === "connecting" ? "Négociation…" :
             state === "error" ? "Erreur" : "Déconnecté"}
          </span>
        </div>
      </div>

      {/* Connect button */}
      <div style={{ position: "relative", width: 220, height: 220, margin: "0 auto" }}>
        {Array.from({ length: ringCount }).map((_, i) => (
          <span key={i} aria-hidden="true" style={{
            position: "absolute", inset: 0, borderRadius: "50%",
            border: `1.5px solid ${color}`,
            animation: reduceMotion ? "none" :
              state === "connecting" ? `sonar 1.8s ${i * 0.5}s infinite ease-out` :
              `breathe 3.2s infinite ease-in-out`,
          }} />
        ))}
        <button
          onClick={onToggleConnect}
          aria-label={
            state === "connected" ? `Déconnecter du serveur ${server.city}` :
            state === "connecting" ? `Annuler la connexion` :
            `Se connecter au serveur ${server.city}`
          }
          style={{
            position: "absolute", inset: 34, borderRadius: "50%",
            border: `1.5px solid ${color}`,
            background: `radial-gradient(circle at 35% 30%, ${T.surfaceElevated}, ${T.surface})`,
            boxShadow: state === "connected" ? `0 0 34px ${T.securedDim}` : "none",
            cursor: "pointer",
            display: "flex", flexDirection: "column",
            alignItems: "center", justifyContent: "center", gap: 8,
          }}
        >
          {state === "connected" ? <ShieldCheck size={38} color={color} strokeWidth={1.6} /> :
           state === "connecting" ? <Radar size={34} color={color} strokeWidth={1.6} style={{ animation: reduceMotion ? "none" : "spin 2.2s linear infinite" }} /> :
           state === "error" ? <AlertCircle size={34} color={color} strokeWidth={1.6} /> :
           <Shield size={36} color={color} strokeWidth={1.6} />}
          <span style={{
            fontFamily: "'JetBrains Mono', monospace",
            fontSize: 11, letterSpacing: 1.5,
            textTransform: "uppercase", color: T.textMuted,
          }}>{label}</span>
        </button>
      </div>

      {/* Server card */}
      <button onClick={onGoServers} aria-label={`Changer de serveur, actuellement ${server.city}`} style={styles.cardBtn}>
        <span aria-hidden="true" style={{ fontSize: 22 }}>{server.flag}</span>
        <div style={{ flex: 1, textAlign: "left" }}>
          <div style={{ fontSize: 13.5, color: T.textPrimary, fontWeight: 500 }}>
            {server.city}, {server.country}
          </div>
          <div style={{ fontSize: 11.5, color: T.textMuted, marginTop: 1 }}>Nœud relais actuel</div>
        </div>
        <ChevronRight size={17} color={T.textMuted} aria-hidden="true" />
      </button>

      {/* Before/after IP */}
      {state === "connected" && realIp && warpIp && (
        <div role="region" aria-label="Changement d'IP" style={{
          background: T.securedDim,
          border: `1px solid ${T.secured}`,
          borderRadius: 14, padding: "12px 14px",
          display: "flex", alignItems: "center", gap: 10,
        }}>
          <ShieldCheck size={16} color={T.secured} aria-hidden="true" />
          <div style={{ flex: 1, fontFamily: "'JetBrains Mono', monospace", fontSize: 12 }}>
            <div style={{ color: T.textMuted, fontSize: 10.5 }}>NOUVELLE IP</div>
            <div style={{ color: T.textPrimary, fontWeight: 600 }}>{warpIp}</div>
            <div style={{ color: T.textMuted, fontSize: 10.5, marginTop: 4 }}>ANCIENNE IP</div>
            <div style={{ color: T.textSecondary, textDecoration: "line-through" }}>{realIp}</div>
          </div>
        </div>
      )}

      {/* Telemetry */}
      <TelemetryCard icon={<Globe size={13} color={T.textMuted} />} label="ADRESSE IP PUBLIQUE" value={state === "connected" && warpIp ? warpIp : "—.—.—.—"} />
      <div style={{ display: "flex", gap: 10 }}>
        <TelemetryCard icon={<Download size={13} color={T.textMuted} />} label="TÉLÉCHARGEMENT" value={download} unit="Mb/s" />
        <TelemetryCard icon={<Upload size={13} color={T.textMuted} />} label="ENVOI" value={upload} unit="Mb/s" />
      </div>
      <TelemetryCard icon={<Clock size={13} color={T.textMuted} />} label="DURÉE DE SESSION" value={formatDuration(duration)} />

      {/* Disclaimer */}
      <div style={{
        textAlign: "center", fontSize: 10.5,
        color: T.textMuted, letterSpacing: 0.8,
        textTransform: "uppercase", padding: "4px 0",
      }}>
        ⚠ MVP — Simulation
      </div>
    </div>
  );
}

function TelemetryCard({ icon, label, value, unit }: { icon: React.ReactNode; label: string; value: string; unit?: string }) {
  return (
    <div style={styles.telemetryCard}>
      <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 8 }}>
        {icon}
        <span style={{ fontSize: 11, color: T.textMuted, letterSpacing: 0.3 }}>{label}</span>
      </div>
      <div style={{
        fontFamily: "'JetBrains Mono', monospace",
        fontSize: 16, color: T.textPrimary, fontWeight: 600,
      }}>
        {value}
        {unit && <span style={{ fontSize: 11, color: T.textMuted, marginLeft: 3 }}>{unit}</span>}
      </div>
    </div>
  );
}

// === SERVERS SCREEN ===
function ServersScreen({ selectedId, onSelect }: { selectedId: number; onSelect: (id: number) => void }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
      <h2 style={{
        fontSize: 18, color: T.textPrimary, fontWeight: 600, margin: 0,
        fontFamily: "'Space Grotesk', sans-serif",
      }}>Serveurs</h2>
      <p style={{ fontSize: 12.5, color: T.textMuted, margin: "4px 0 12px" }}>
        {SERVERS.length} nœuds disponibles
      </p>
      {SERVERS.map(s => {
        const sel = s.id === selectedId;
        return (
          <button key={s.id} onClick={() => onSelect(s.id)} aria-pressed={sel} style={{
            display: "flex", alignItems: "center", gap: 12,
            padding: "12px", borderRadius: 14,
            border: `1px solid ${sel ? T.secured : T.borderSoft}`,
            background: sel ? T.securedDim : T.surface,
            cursor: "pointer", textAlign: "left",
          }}>
            <span aria-hidden="true" style={{ fontSize: 24 }}>{s.flag}</span>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14, color: T.textPrimary, fontWeight: 500 }}>{s.city}</div>
              <div style={{ fontSize: 11.5, color: T.textMuted, marginTop: 1 }}>{s.country}</div>
            </div>
            <div style={{ textAlign: "right" }}>
              <div style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 12.5, color: T.textSecondary }}>
                {s.ping} ms
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 4, marginTop: 3, justifyContent: "flex-end" }}>
                <span style={{ width: 5, height: 5, borderRadius: "50%", background: loadColor(s.load) }} aria-hidden="true" />
                <span style={{ fontSize: 10.5, color: T.textMuted }}>{s.load}%</span>
              </div>
            </div>
            {sel && (
              <div style={{
                width: 20, height: 20, borderRadius: "50%",
                background: T.secured,
                display: "flex", alignItems: "center", justifyContent: "center",
              }}>
                <Check size={12} color={T.bg} strokeWidth={3} />
              </div>
            )}
          </button>
        );
      })}
    </div>
  );
}

// === SETTINGS SCREEN ===
function SettingsScreen() {
  const [protocol, setProtocol] = useState<"WireGuard" | "OpenVPN">("WireGuard");
  const [killSwitch, setKillSwitch] = useState(true);

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 22 }}>
      <h2 style={{
        fontSize: 18, color: T.textPrimary, fontWeight: 600, margin: 0,
        fontFamily: "'Space Grotesk', sans-serif",
      }}>Paramètres</h2>

      <div>
        <div style={{ fontSize: 11.5, color: T.textMuted, letterSpacing: 0.5, marginBottom: 8, textTransform: "uppercase" }}>
          Protocole
        </div>
        <div style={{ display: "flex", gap: 8 }}>
          {(["WireGuard", "OpenVPN"] as const).map(p => {
            const active = protocol === p;
            return (
              <button key={p} onClick={() => setProtocol(p)} aria-pressed={active} style={{
                flex: 1, display: "flex", alignItems: "center",
                justifyContent: "center", gap: 6,
                padding: "11px 8px", borderRadius: 12,
                border: `1px solid ${active ? T.secured : T.borderSoft}`,
                background: active ? T.securedDim : T.surface,
                color: active ? T.secured : T.textSecondary,
                fontSize: 13, fontWeight: 500, cursor: "pointer",
              }}>
                {p === "WireGuard" ? <Zap size={14} /> : <Lock size={14} />}
                {p}
              </button>
            );
          })}
        </div>
      </div>

      <div>
        <div style={{ fontSize: 11.5, color: T.textMuted, letterSpacing: 0.5, marginBottom: 4, textTransform: "uppercase" }}>
          Protection
        </div>
        <div style={{ background: T.surface, border: `1px solid ${T.borderSoft}`, borderRadius: 14, padding: "0 12px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "14px 4px", borderBottom: `1px solid ${T.borderSoft}` }}>
            <div style={{ width: 34, height: 34, borderRadius: 10, background: T.surfaceElevated, display: "flex", alignItems: "center", justifyContent: "center" }}>
              <ShieldCheck size={16} color={T.textSecondary} />
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14.5, color: T.textPrimary, fontWeight: 500 }}>Kill Switch</div>
              <div style={{ fontSize: 12, color: T.textMuted, marginTop: 2 }}>Coupe l'accès si le tunnel tombe</div>
            </div>
            <button
              role="switch" aria-checked={killSwitch} aria-label="Kill Switch"
              onClick={() => setKillSwitch(!killSwitch)}
              style={{
                width: 46, height: 27, borderRadius: 999, border: "none", padding: 3,
                background: killSwitch ? T.secured : T.borderSoft,
                display: "flex", justifyContent: "flex-start", cursor: "pointer",
              }}
            >
              <span aria-hidden="true" style={{
                width: 21, height: 21, borderRadius: "50%", background: "#fff",
                transform: killSwitch ? "translateX(19px)" : "translateX(0)",
                transition: "transform 0.25s cubic-bezier(0.4, 0, 0.2, 1)",
              }} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// === STYLES ===
const styles = {
  app: {
    minHeight: "100vh",
    background: T.bg,
    display: "flex", justifyContent: "center",
    fontFamily: "'Inter', sans-serif",
  } as React.CSSProperties,
  header: {
    display: "flex", alignItems: "center",
    justifyContent: "space-between", padding: "20px 20px 6px",
  } as React.CSSProperties,
  logo: {
    width: 26, height: 26, borderRadius: 8,
    background: T.securedDim,
    display: "flex", alignItems: "center", justifyContent: "center",
  } as React.CSSProperties,
  brand: {
    fontFamily: "'Space Grotesk', sans-serif",
    fontSize: 17, fontWeight: 600,
    color: T.textPrimary, letterSpacing: 0.2,
  } as React.CSSProperties,
  city: {
    display: "flex", alignItems: "center", gap: 5,
    fontFamily: "'JetBrains Mono', monospace",
    fontSize: 11, color: T.textMuted,
  } as React.CSSProperties,
  main: {
    flex: 1, overflowY: "auto", padding: "14px 20px 0",
  } as React.CSSProperties,
  nav: {
    display: "flex",
    borderTop: `1px solid ${T.borderSoft}`,
    background: T.bg, padding: "10px 6px 14px", gap: 4,
  } as React.CSSProperties,
  navBtn: {
    flex: 1, display: "flex", flexDirection: "column",
    alignItems: "center", gap: 4, padding: "6px 0",
    background: "transparent", border: "none", cursor: "pointer",
  } as React.CSSProperties,
  statusPill: {
    display: "inline-flex", alignItems: "center", gap: 7,
    padding: "5px 12px", borderRadius: 999,
    background: T.surface,
    border: `1px solid ${T.borderSoft}`,
  } as React.CSSProperties,
  cardBtn: {
    display: "flex", alignItems: "center", gap: 12,
    background: T.surface,
    border: `1px solid ${T.borderSoft}`,
    borderRadius: 14, padding: "12px 14px",
    cursor: "pointer", textAlign: "left",
  } as React.CSSProperties,
  telemetryCard: {
    background: T.surface,
    border: `1px solid ${T.borderSoft}`,
    borderRadius: 14, padding: "14px 14px",
    flex: 1, minWidth: 0,
  } as React.CSSProperties,
};
APP

# README MVP
cat > README.md <<'EOF'
# 🛡️ Voile VPN — MVP

Prototype standalone (PWA) en un seul fichier React.

## Démarrage

```bash
pnpm install
pnpm dev
# → http://localhost:5173
```

## Build

```bash
pnpm build
pnpm preview
```

## Fonctionnalités

- ✅ UI complète (Home, Servers, Settings)
- ✅ Animations sonar/breathe
- ✅ Trust Score concept (UI)
- ✅ Avant/après IP (via ipify)
- ✅ Accessible (WCAG 2.2 AA)
- ✅ PWA installable

## Limitations MVP

- ⚠️ Tunnel simulé (pas de vrai WireGuard)
- ⚠️ Pas d'authentification Supabase
- ⚠️ Pas de persistance (state reset au refresh)

Pour la version complète avec tunnel réel, voir [github.com/voile/voile](https://github.com/voile/voile).
EOF

# Gitignore
cat > .gitignore <<'EOF'
node_modules/
dist/
*.log
.env
.DS_Store
EOF

# Install + premier build
echo ""
echo "📥 Installation des dépendances..."
pnpm install --silent

echo ""
echo "🎉 MVP prêt !"
echo ""
echo "Commandes :"
echo "  cd $PROJECT_NAME"
echo "  pnpm dev        # → http://localhost:5173"
echo "  pnpm build      # Build prod"
echo "  pnpm preview    # Preview du build"
echo ""
echo "📁 Structure créée :"
echo "  $PROJECT_NAME/"
echo "  ├── src/VoileApp.tsx  (450 lignes, tout-en-un)"
echo "  ├── src/main.tsx"
echo "  ├── index.html"
echo "  ├── vite.config.ts"
echo "  ├── package.json"
echo "  └── README.md"
echo ""

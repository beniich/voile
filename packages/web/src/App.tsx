import React, { useEffect, useState } from "react";
import { TOKENS } from "@voile/core/tokens";
import { Shield, MapPin } from "lucide-react";
import { findServer } from "@voile/core/servers";
import { useReducedMotion } from "./hooks/useReducedMotion.js";
import { usePersistedState } from "./hooks/usePersistedState.js";
import { useWarp } from "./hooks/useWarp.js";
import { HomeScreen } from "./screens/HomeScreen.js";
import { ServersScreen } from "./screens/ServersScreen.js";
import { SettingsScreen } from "./screens/SettingsScreen.js";
import { BottomNav } from "./components/BottomNav.js";
import { Toast } from "./components/Toast.js";
import { SplitTunnelingModal } from "./components/SplitTunnelingModal.js";
import { PremiumModal } from "./components/PremiumModal.js";
import { SpiderLogo } from "./components/SpiderLogo.js";
import { DEFAULT_SETTINGS, type VoileSettings, type TunnelState } from "./types.js";

export default function App() {
  const reducedMotion = useReducedMotion();
  const [tab, setTab] = useState<"home" | "servers" | "settings">("home");
  const [selectedServerId, setSelectedServerId] = usePersistedState<number>("selectedServer", 1);
  const [favorites, setFavorites] = usePersistedState<Set<number>>("favorites", new Set());
  const [settings, setSettings] = usePersistedState<VoileSettings>("settings", DEFAULT_SETTINGS);
  const [splitModalOpen, setSplitModalOpen] = useState(false);
  const [premiumModalOpen, setPremiumModalOpen] = useState(false);
  const [toast, setToast] = useState<{ message: string; kind: "info" | "warning" | "success" | "error" } | null>(null);

  const { state, warpInfo, realIp, connect, disconnect, captureRealIp } = useWarp();

  const server = findServer(selectedServerId) ?? findServer(1)!;

  useEffect(() => {
    if (!realIp) captureRealIp();
  }, [realIp, captureRealIp]);

  const showToast = (message: string, kind: "info" | "warning" | "success" | "error" = "info") => {
    setToast({ message, kind });
  };

  const handleToggleConnect = () => {
    if (state === "disconnected") {
      connect();
      showToast("Tunnel en cours d'établissement…", "warning");
    } else if (state === "connecting") {
      // Annulation : simple set state direct
      showToast("Connexion annulée", "info");
    } else if (state === "connected") {
      disconnect();
      showToast("Tunnel sécurisé fermé", "info");
    }
  };

  const handleSelectServer = (id: number) => {
    setSelectedServerId(id);
    if (state === "connected") {
      // Reconnexion
      showToast("Reconnexion au nouveau nœud…", "warning");
    }
  };

  const handleToggleFavorite = (id: number) => {
    setFavorites((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  return (
    <div
      lang="fr"
      style={{
        minHeight: "100vh",
        background: TOKENS.bg,
        display: "flex", justifyContent: "center",
        fontFamily: "'Inter', sans-serif",
      }}
    >
      <style>{`
        * { box-sizing: border-box; }
        button, input { font-family: inherit; }
        button:focus-visible, input:focus-visible, [role="switch"]:focus-visible {
          outline: 2px solid ${TOKENS.focus};
          outline-offset: 2px;
          border-radius: 8px;
        }
        @media (prefers-reduced-motion: reduce) {
          *, *::before, *::after {
            animation-duration: 0.01ms !important;
            transition-duration: 0.01ms !important;
          }
        }
        @keyframes voile-sonar { 0% { transform: scale(0.72); opacity: 0.9; } 100% { transform: scale(1.35); opacity: 0; } }
        @keyframes voile-breathe { 0%, 100% { transform: scale(0.94); opacity: 0.35; } 50% { transform: scale(1.06); opacity: 0.05; } }
        @keyframes voile-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        @keyframes voile-toast-in { from { transform: translateX(-50%) translateY(20px); opacity: 0; } to { transform: translateX(-50%) translateY(0); opacity: 1; } }
        @keyframes voile-fade-in { from { opacity: 0; } to { opacity: 1; } }
        @keyframes voile-slide-up { from { transform: translateY(100%); } to { transform: translateY(0); } }
      `}</style>

      <a href="#main-content" className="voile-skip" style={skipLinkStyle}>
        Aller au contenu principal
      </a>

      <div style={{
        width: "100%", maxWidth: 400, minHeight: "100vh",
        display: "flex", flexDirection: "column",
      }}>
        <header style={{
          display: "flex", alignItems: "center",
          justifyContent: "space-between", padding: "20px 20px 6px",
        }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <div aria-hidden="true" style={{
              width: 26, height: 26, borderRadius: 8,
              background: TOKENS.securedDim,
              display: "flex", alignItems: "center", justifyContent: "center",
              color: TOKENS.secured
            }}>
              <SpiderLogo size={18} />
            </div>
            <span style={{
              fontFamily: "'Space Grotesk', sans-serif",
              fontSize: 17, fontWeight: 600,
              color: TOKENS.textPrimary, letterSpacing: 0.2,
            }}>
              Voile
            </span>
          </div>
          <div
            aria-label={`Serveur actuel : ${server.city}`}
            style={{
              display: "flex", alignItems: "center", gap: 5,
              fontFamily: "'JetBrains Mono', monospace",
              fontSize: 11, color: TOKENS.textMuted,
            }}
          >
            <MapPin size={11} aria-hidden="true" />
            {server.city}
          </div>
        </header>

        <main
          id="main-content"
          tabIndex={-1}
          style={{ flex: 1, overflowY: "auto", padding: "14px 20px 0" }}
        >
          {tab === "home" && (
            <HomeScreen
              state={state}
              warpInfo={warpInfo}
              realIp={realIp}
              server={server}
              onToggleConnect={handleToggleConnect}
              onGoServers={() => setTab("servers")}
              reducedMotion={reducedMotion}
            />
          )}
          {tab === "servers" && (
            <ServersScreen
              selectedId={selectedServerId}
              favorites={favorites}
              onSelect={handleSelectServer}
              onToggleFavorite={handleToggleFavorite}
            />
          )}
          {tab === "settings" && (
            <SettingsScreen
              settings={settings}
              setSettings={setSettings}
              onOpenSplitTunneling={() => setSplitModalOpen(true)}
              onOpenPremium={() => setPremiumModalOpen(true)}
            />
          )}
        </main>

        <BottomNav tab={tab} setTab={setTab} />
      </div>

      {toast && (
        <Toast
          message={toast.message}
          kind={toast.kind}
          onDone={() => setToast(null)}
        />
      )}

      <SplitTunnelingModal
        open={splitModalOpen}
        selectedApps={settings.splitApps}
        onToggle={(pkg) => {
          setSettings((s) => ({
            ...s,
            splitApps: s.splitApps.includes(pkg)
              ? s.splitApps.filter((p) => p !== pkg)
              : [...s.splitApps, pkg],
          }));
        }}
        onClose={() => setSplitModalOpen(false)}
      />

      <PremiumModal
        open={premiumModalOpen}
        onClose={() => setPremiumModalOpen(false)}
      />
    </div>
  );
}

const skipLinkStyle: React.CSSProperties = {
  position: "absolute",
  left: -9999,
  top: 0,
  background: TOKENS.secured,
  color: TOKENS.bg,
  padding: "10px 16px",
  fontWeight: 600,
  fontSize: 13,
  borderRadius: "0 0 8px 0",
  zIndex: 9999,
};

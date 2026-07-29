import React from "react";
import { Zap, Lock, ShieldCheck, Wifi, Radar, Globe, AlertCircle, Sparkles } from "lucide-react";
import { TOKENS } from "@voile/core/tokens";
import { fr } from "@voile/core/i18n";
import { ToggleSwitch } from "../components/ToggleSwitch.js";
import { SettingsRow } from "../components/SettingsRow.js";
import type { VoileSettings } from "../types.js";

interface SettingsScreenProps {
  settings: VoileSettings;
  setSettings: React.Dispatch<React.SetStateAction<VoileSettings>>;
  onOpenSplitTunneling: () => void;
  onOpenPremium: () => void;
}

export function SettingsScreen({
  settings, setSettings, onOpenSplitTunneling, onOpenPremium
}: SettingsScreenProps) {
  const update = <K extends keyof VoileSettings>(key: K) =>
    (val: VoileSettings[K]) => setSettings((s) => ({ ...s, [key]: val }));

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 22, paddingBottom: 8 }}>
      <header>
        <h2 style={{
          fontSize: 18, color: TOKENS.textPrimary, fontWeight: 600, margin: 0,
          fontFamily: "'Space Grotesk', sans-serif",
        }}>
          Paramètres
        </h2>
        <p style={{ fontSize: 12.5, color: TOKENS.textMuted, margin: "4px 0 0" }}>
          Sécurité et comportement du tunnel
        </p>
      </header>

      {/* Voile+ CTA */}
      <button
        onClick={onOpenPremium}
        aria-label="Découvrir Voile+, passer à la version Premium"
        style={{
          display: "flex", alignItems: "center", gap: 12,
          background: `linear-gradient(135deg, ${TOKENS.surface} 0%, ${TOKENS.surfaceElevated} 100%)`,
          border: `1px solid ${TOKENS.secured}`,
          borderRadius: 14, padding: "14px 16px",
          cursor: "pointer", textAlign: "left",
          boxShadow: `0 4px 14px ${TOKENS.securedDim}`,
        }}
      >
        <div style={{
          width: 36, height: 36, borderRadius: "50%",
          background: TOKENS.secured,
          display: "flex", alignItems: "center", justifyContent: "center",
        }}>
          <Sparkles size={18} color={TOKENS.bg} />
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 15, color: TOKENS.secured, fontWeight: 600 }}>Voile+ Premium</div>
          <div style={{ fontSize: 12, color: TOKENS.textSecondary, marginTop: 2 }}>
            Multi-hop, IP dédiée, jusqu'à 5 appareils
          </div>
        </div>
      </button>

      {/* Protocol selector */}
      <fieldset style={{ border: "none", padding: 0, margin: 0 }}>
        <legend style={{
          fontSize: 11.5, color: TOKENS.textMuted, letterSpacing: 0.5,
          marginBottom: 8, textTransform: "uppercase",
        }}>
          Protocole
        </legend>
        <div style={{ display: "flex", gap: 8 }}>
          {(["WireGuard", "OpenVPN"] as const).map((p) => {
            const active = settings.protocol === p;
            return (
              <button
                key={p}
                onClick={() => update("protocol")(p)}
                aria-pressed={active}
                aria-label={`Protocole ${p}${active ? ", sélectionné" : ""}`}
                style={{
                  flex: 1, display: "flex", alignItems: "center",
                  justifyContent: "center", gap: 6,
                  padding: "11px 8px", borderRadius: 12,
                  border: `1px solid ${active ? TOKENS.secured : TOKENS.borderSoft}`,
                  background: active ? TOKENS.securedDim : TOKENS.surface,
                  color: active ? TOKENS.secured : TOKENS.textSecondary,
                  fontSize: 13, fontWeight: 500, cursor: "pointer",
                }}
              >
                <span aria-hidden="true">
                  {p === "WireGuard" ? <Zap size={14} /> : <Lock size={14} />}
                </span>
                {p}
              </button>
            );
          })}
        </div>
      </fieldset>

      {/* Protection toggles */}
      <div>
        <h3 style={{
          fontSize: 11.5, color: TOKENS.textMuted, letterSpacing: 0.5,
          margin: "0 0 4px", textTransform: "uppercase", fontWeight: 500,
        }}>
          Protection
        </h3>
        <div style={{
          background: TOKENS.surface,
          border: `1px solid ${TOKENS.borderSoft}`,
          borderRadius: 14, padding: "0 12px",
        }}>
          <SettingsRow
            icon={ShieldCheck}
            title="Kill Switch"
            subtitle="Coupe l'accès internet si le tunnel se déconnecte"
            htmlFor="kill-switch"
            right={
              <ToggleSwitch
                label="Kill Switch"
                checked={settings.killSwitch}
                onChange={update("killSwitch")}
              />
            }
          />
          <SettingsRow
            icon={Wifi}
            title="Connexion automatique"
            subtitle="Se connecte au serveur le plus proche au démarrage"
            htmlFor="auto-connect"
            right={
              <ToggleSwitch
                label="Connexion automatique"
                checked={settings.autoConnect}
                onChange={update("autoConnect")}
              />
            }
          />
          <SettingsRow
            icon={Radar}
            title="CyberSec Shield"
            subtitle="Bloque les fuites DNS et le suivi publicitaire"
            htmlFor="cybersec"
            right={
              <ToggleSwitch
                label="CyberSec Shield"
                checked={settings.cyberSec}
                onChange={update("cyberSec")}
              />
            }
          />
          <div style={{ borderBottom: "none" }}>
            <SettingsRow
              icon={Globe}
              title="Split tunneling"
              subtitle={
                settings.splitTunneling
                  ? `${settings.splitApps.length} application${settings.splitApps.length > 1 ? "s" : ""} exclue${settings.splitApps.length > 1 ? "s" : ""}`
                  : "Choisir les apps qui contournent le VPN"
              }
              htmlFor="split-tunneling"
              right={
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  {settings.splitTunneling && (
                    <button
                      onClick={onOpenSplitTunneling}
                      aria-label="Configurer les applications exclues du VPN"
                      style={{
                        background: TOKENS.surfaceElevated,
                        border: `1px solid ${TOKENS.borderSoft}`,
                        borderRadius: 8, color: TOKENS.textSecondary,
                        fontSize: 11.5, padding: "5px 10px", cursor: "pointer",
                      }}
                    >
                      Configurer
                    </button>
                  )}
                  <ToggleSwitch
                    label="Split tunneling"
                    checked={settings.splitTunneling}
                    onChange={update("splitTunneling")}
                  />
                </div>
              }
            />
          </div>
        </div>
      </div>

      {settings.protocol === "OpenVPN" && (
        <div
          role="note"
          style={{
            background: TOKENS.connectingDim,
            border: `1px solid ${TOKENS.connecting}`,
            borderRadius: 12, padding: "10px 12px",
            fontSize: 12, color: TOKENS.textSecondary,
            display: "flex", gap: 8, alignItems: "flex-start",
          }}
        >
          <AlertCircle size={14} color={TOKENS.connecting} style={{ marginTop: 1, flexShrink: 0 }} aria-hidden="true" />
          <span>OpenVPN est plus lent mais plus compatible avec les réseaux restreints.</span>
        </div>
      )}
    </div>
  );
}

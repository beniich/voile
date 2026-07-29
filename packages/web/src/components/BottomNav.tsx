import React from "react";
import { Home, Globe, Settings as SettingsIcon } from "lucide-react";
import { TOKENS } from "@voile/core/tokens";

interface BottomNavProps {
  tab: "home" | "servers" | "settings";
  setTab: (tab: "home" | "servers" | "settings") => void;
}

const ITEMS = [
  { id: "home" as const, label: "Accueil", icon: Home },
  { id: "servers" as const, label: "Serveurs", icon: Globe },
  { id: "settings" as const, label: "Réglages", icon: SettingsIcon },
];

export function BottomNav({ tab, setTab }: BottomNavProps) {
  return (
    <nav aria-label="Navigation principale">
      <div style={{
        display: "flex", borderTop: `1px solid ${TOKENS.borderSoft}`,
        background: TOKENS.bg, padding: "10px 6px 14px", gap: 4,
      }}>
        {ITEMS.map((it) => {
          const active = tab === it.id;
          const Icon = it.icon;
          return (
            <button
              key={it.id}
              onClick={() => setTab(it.id)}
              aria-label={it.label}
              aria-current={active ? "page" : undefined}
              style={{
                flex: 1, display: "flex", flexDirection: "column",
                alignItems: "center", gap: 4, padding: "6px 0",
                background: "transparent", border: "none", cursor: "pointer",
              }}
            >
              <Icon
                size={19}
                color={active ? TOKENS.secured : TOKENS.textMuted}
                strokeWidth={active ? 2.1 : 1.8}
                aria-hidden="true"
              />
              <span style={{
                fontSize: 10.5,
                color: active ? TOKENS.secured : TOKENS.textMuted,
                fontWeight: active ? 600 : 400,
              }}>
                {it.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}

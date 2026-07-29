import React from "react";
import { Globe, Download, Upload, Clock } from "lucide-react";
import { TOKENS } from "@voile/core/tokens";

export type TelemetryIcon = "globe" | "download" | "upload" | "clock";

interface TelemetryCardProps {
  icon: TelemetryIcon;
  label: string;
  value: string;
  unit?: string;
  id?: string;
}

const ICONS: Record<TelemetryIcon, typeof Globe> = {
  globe: Globe,
  download: Download,
  upload: Upload,
  clock: Clock,
};

export function TelemetryCard({ icon, label, value, unit, id }: TelemetryCardProps) {
  const Icon = ICONS[icon];
  const ariaLabel = `${label} : ${value}${unit ? " " + unit : ""}`;

  return (
    <div
      style={{
        background: TOKENS.surface,
        border: `1px solid ${TOKENS.borderSoft}`,
        borderRadius: 14,
        padding: "14px 14px",
        flex: 1,
        minWidth: 0,
      }}
      aria-labelledby={id}
    >
      <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 8 }}>
        <Icon size={13} color={TOKENS.textMuted} strokeWidth={2} aria-hidden="true" />
        <span id={id} style={{ fontSize: 11, color: TOKENS.textMuted, letterSpacing: 0.3 }}>
          {label}
        </span>
      </div>
      <div
        aria-label={ariaLabel}
        style={{
          fontFamily: "'JetBrains Mono', monospace",
          fontSize: 16,
          color: TOKENS.textPrimary,
          fontWeight: 600,
          whiteSpace: "nowrap",
          overflow: "hidden",
          textOverflow: "ellipsis",
        }}
      >
        <span aria-hidden="true">
          {value}
          {unit && (
            <span style={{ fontSize: 11, color: TOKENS.textMuted, marginLeft: 3 }}>
              {unit}
            </span>
          )}
        </span>
      </div>
    </div>
  );
}

import React from "react";
import { TOKENS } from "@voile/core/tokens";

interface SettingsRowProps {
  icon: React.ComponentType<{ size?: number; color?: string; strokeWidth?: number }>;
  title: string;
  subtitle?: string;
  htmlFor: string;
  right: React.ReactElement<{ id?: string; "aria-describedby"?: string }>;
}

export function SettingsRow({ icon: Icon, title, subtitle, htmlFor, right }: SettingsRowProps) {
  const titleId = `row-title-${htmlFor}`;
  const subtitleId = `row-sub-${htmlFor}`;

  return (
    <div style={{
      display: "flex", alignItems: "center", gap: 12,
      padding: "14px 4px",
      borderBottom: `1px solid ${TOKENS.borderSoft}`,
    }}>
      <div
        aria-hidden="true"
        style={{
          width: 34, height: 34, borderRadius: 10,
          background: TOKENS.surfaceElevated,
          display: "flex", alignItems: "center", justifyContent: "center",
          flexShrink: 0,
        }}
      >
        <Icon size={16} color={TOKENS.textSecondary} strokeWidth={2} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <label
          htmlFor={htmlFor}
          id={titleId}
          style={{
            fontSize: 14.5, color: TOKENS.textPrimary,
            fontWeight: 500, display: "block", cursor: "pointer",
          }}
        >
          {title}
        </label>
        {subtitle && (
          <div id={subtitleId} style={{
            fontSize: 12, color: TOKENS.textMuted,
            marginTop: 2, lineHeight: 1.4,
          }}>
            {subtitle}
          </div>
        )}
      </div>
      {React.cloneElement(right, {
        id: htmlFor,
        "aria-describedby": subtitle ? subtitleId : undefined,
      })}
    </div>
  );
}

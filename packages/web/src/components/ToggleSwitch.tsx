import React from "react";
import { TOKENS } from "@voile/core/tokens";

interface ToggleSwitchProps {
  checked: boolean;
  onChange: (next: boolean) => void;
  disabled?: boolean;
  label: string;
}

export function ToggleSwitch({ checked, onChange, disabled, label }: ToggleSwitchProps) {
  return (
    <button
      onClick={() => !disabled && onChange(!checked)}
      role="switch"
      aria-checked={checked}
      aria-label={label}
      aria-disabled={disabled}
      style={{
        width: 46, height: 27, borderRadius: 999,
        border: "none", padding: 3,
        cursor: disabled ? "not-allowed" : "pointer",
        background: checked ? TOKENS.secured : TOKENS.borderSoft,
        opacity: disabled ? 0.4 : 1,
        display: "flex", justifyContent: "flex-start",
        transition: "background 0.25s ease",
        flexShrink: 0,
      }}
    >
      <span
        aria-hidden="true"
        style={{
          width: 21, height: 21, borderRadius: "50%",
          background: "#fff",
          display: "block",
          boxShadow: "0 1px 3px rgba(0,0,0,0.4)",
          transform: checked ? "translateX(19px)" : "translateX(0)",
          transition: "transform 0.25s cubic-bezier(0.4, 0, 0.2, 1)",
        }}
      />
    </button>
  );
}

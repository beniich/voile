import React, { useEffect } from "react";
import { TOKENS } from "@voile/core/tokens";

interface ToastProps {
  message: string;
  kind: "info" | "warning" | "success" | "error";
  onDone: () => void;
}

export function Toast({ message, kind, onDone }: ToastProps) {
  useEffect(() => {
    const t = setTimeout(onDone, 2400);
    return () => clearTimeout(t);
  }, [onDone]);

  const color =
    kind === "success" ? TOKENS.secured :
    kind === "warning" ? TOKENS.connecting :
    kind === "error" ? TOKENS.danger :
    TOKENS.accentBlue;

  const ariaLive = kind === "error" ? "assertive" : "polite";

  return (
    <div
      role="status"
      aria-live={ariaLive}
      aria-atomic="true"
      style={{
        position: "fixed",
        bottom: 90, left: "50%", transform: "translateX(-50%)",
        background: TOKENS.surfaceElevated,
        border: `1px solid ${color}`,
        color: TOKENS.textPrimary,
        padding: "10px 16px", borderRadius: 12,
        fontSize: 13, fontWeight: 500,
        display: "flex", alignItems: "center", gap: 8,
        boxShadow: "0 8px 24px rgba(0,0,0,0.4)",
        zIndex: 1000,
        animation: "voile-toast-in 0.25s ease-out",
      }}
    >
      <span
        aria-hidden="true"
        style={{ width: 6, height: 6, borderRadius: "50%", background: color }}
      />
      {message}
    </div>
  );
}

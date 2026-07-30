// Design tokens partagés — source unique de vérité pour Web et Android.
// Le fichier codegen.ts génère la version Kotlin à partir de cet objet.

export const TOKENS = {
  // Backgrounds
  bg:              "#040D12",
  surface:         "#0B1722",
  surfaceElevated: "#132333",

  // Borders
  border:     "#1E3347",
  borderSoft: "#132333",

  // Text
  textPrimary:   "#F8FAFC",
  textSecondary: "#94A3B8",
  textMuted:     "#7A8499",

  // State colors
  idle:         "#7A8499",
  connecting:   "#F5A623",
  secured:      "#10B981",
  danger:       "#EF4444",
  accentBlue:   "#3B82F6",

  // Dim variants (RGBA — exclus du codegen Kotlin, gérés manuellement)
  securedDim:    "rgba(16, 185, 129, 0.125)",
  connectingDim: "rgba(245, 166, 35, 0.125)",
  errorDim:      "rgba(239, 68, 68, 0.125)",
  // Focus outline
  focus:        "#3B82F6",
} as const;

export type TokenKey = keyof typeof TOKENS;
export type TokenValue = (typeof TOKENS)[TokenKey];

import React from "react";
import { ChevronRight, Check, Star } from "lucide-react";
import { TOKENS } from "@voile/core/tokens";
import type { Server } from "../types.js";

interface ServerCardProps {
  server: Server;
  favorite?: boolean;
  selected?: boolean;
  showFavoriteToggle?: boolean;
  compact?: boolean;
  onClick?: () => void;
  onToggleFavorite?: () => void;
}

export function ServerCard({
  server, favorite, selected, showFavoriteToggle, compact, onClick, onToggleFavorite,
}: ServerCardProps) {
  const isSelected = selected ?? false;
  const isFavorite = favorite ?? false;

  const buttonContent = (
    <>
      <span aria-hidden="true" style={{ fontSize: compact ? 22 : 24 }}>
        {server.flag}
      </span>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{
          fontSize: compact ? 13.5 : 14,
          color: TOKENS.textPrimary,
          fontWeight: 500,
          whiteSpace: "nowrap",
          overflow: "hidden",
          textOverflow: "ellipsis",
        }}>
          {server.city}, {server.country}
        </div>
        <div style={{
          fontSize: compact ? 11.5 : 11,
          color: TOKENS.textMuted,
          marginTop: 1,
        }}>
          {compact ? "Nœud relais actuel" : `${server.country}`}
        </div>
      </div>
      {compact ? (
        <ChevronRight size={17} color={TOKENS.textMuted} aria-hidden="true" />
      ) : isSelected ? (
        <div
          aria-hidden="true"
          style={{
            width: 20, height: 20, borderRadius: "50%",
            background: TOKENS.secured,
            display: "flex", alignItems: "center", justifyContent: "center",
            flexShrink: 0,
          }}
        >
          <Check size={12} color={TOKENS.bg} strokeWidth={3} />
        </div>
      ) : null}
    </>
  );

  const ariaLabel = compact
    ? `Changer de serveur, actuellement ${server.city}, ${server.country}`
    : `Serveur ${server.city}, ${server.country}, ping ${server.ping} ms, charge ${server.load}%`;

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 12,
        padding: compact ? "12px 14px" : "12px 12px",
        borderRadius: 14,
        border: `1px solid ${isSelected ? TOKENS.secured : TOKENS.borderSoft}`,
        background: isSelected ? TOKENS.securedDim : TOKENS.surface,
      }}
    >
      {showFavoriteToggle && (
        <button
          onClick={(e) => { e.stopPropagation(); onToggleFavorite?.(); }}
          aria-label={isFavorite ? `Retirer ${server.city} des favoris` : `Ajouter ${server.city} aux favoris`}
          aria-pressed={isFavorite}
          style={{
            background: "transparent",
            border: "none",
            cursor: "pointer",
            padding: 4,
            display: "flex",
            flexShrink: 0,
          }}
        >
          <Star
            size={16}
            color={isFavorite ? TOKENS.connecting : TOKENS.textMuted}
            fill={isFavorite ? TOKENS.connecting : "none"}
            aria-hidden="true"
          />
        </button>
      )}

      <button
        onClick={onClick}
        aria-label={ariaLabel}
        aria-pressed={isSelected}
        style={{
          flex: 1,
          display: "flex",
          alignItems: "center",
          gap: 12,
          background: "transparent",
          border: "none",
          cursor: "pointer",
          textAlign: "left",
          padding: 0,
          minWidth: 0,
          color: "inherit",
        }}
      >
        {buttonContent}
      </button>
    </div>
  );
}

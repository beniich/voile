import React, { useState, useMemo } from "react";
import { Search, X, Star } from "lucide-react";
import { TOKENS } from "@voile/core/tokens";
import { SERVERS, type Server } from "@voile/core/servers";
import { ServerCard } from "../components/ServerCard.js";

interface ServersScreenProps {
  selectedId: number;
  favorites: Set<number>;
  onSelect: (id: number) => void;
  onToggleFavorite: (id: number) => void;
}

export function ServersScreen({
  selectedId, favorites, onSelect, onToggleFavorite,
}: ServersScreenProps) {
  const [query, setQuery] = useState("");
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);

  const filtered = useMemo(() => {
    return SERVERS.filter((s) => {
      const matchesQuery =
        !query ||
        s.city.toLowerCase().includes(query.toLowerCase()) ||
        s.country.toLowerCase().includes(query.toLowerCase());
      const matchesFav = !showFavoritesOnly || favorites.has(s.id);
      return matchesQuery && matchesFav;
    });
  }, [query, showFavoritesOnly, favorites]);

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 6, paddingBottom: 8 }}>
      <header>
        <h2 style={{
          fontSize: 18, color: TOKENS.textPrimary, fontWeight: 600, margin: 0,
          fontFamily: "'Space Grotesk', sans-serif",
        }}>
          Serveurs
        </h2>
        <p style={{ fontSize: 12.5, color: TOKENS.textMuted, margin: "4px 0 0" }}>
          {filtered.length} nœud{filtered.length > 1 ? "s" : ""} disponible{filtered.length > 1 ? "s" : ""}
        </p>
      </header>

      <div role="search" style={{
        display: "flex", alignItems: "center", gap: 8,
        background: TOKENS.surface,
        border: `1px solid ${TOKENS.borderSoft}`,
        borderRadius: 12, padding: "8px 12px",
      }}>
        <Search size={14} color={TOKENS.textMuted} aria-hidden="true" />
        <input
          type="search"
          inputMode="search"
          autoComplete="off"
          placeholder="Rechercher un pays ou une ville"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          aria-label="Rechercher un serveur"
          style={{
            flex: 1, background: "transparent", border: "none",
            outline: "none", color: TOKENS.textPrimary,
            fontSize: 13, fontFamily: "inherit",
          }}
        />
        {query && (
          <button
            onClick={() => setQuery("")}
            aria-label="Effacer la recherche"
            style={{
              background: "transparent", border: "none",
              cursor: "pointer", padding: 0, display: "flex",
            }}
          >
            <X size={14} color={TOKENS.textMuted} aria-hidden="true" />
          </button>
        )}
      </div>

      <button
        onClick={() => setShowFavoritesOnly(!showFavoritesOnly)}
        aria-pressed={showFavoritesOnly}
        style={{
          display: "inline-flex", alignItems: "center", gap: 6,
          padding: "8px 12px",
          background: showFavoritesOnly ? TOKENS.securedDim : TOKENS.surface,
          border: `1px solid ${showFavoritesOnly ? TOKENS.secured : TOKENS.borderSoft}`,
          borderRadius: 999,
          color: showFavoritesOnly ? TOKENS.secured : TOKENS.textSecondary,
          fontSize: 12.5, fontWeight: 500,
          cursor: "pointer", alignSelf: "flex-start",
        }}
      >
        <Star
          size={13}
          fill={showFavoritesOnly ? TOKENS.secured : "none"}
          aria-hidden="true"
        />
        Favoris uniquement
      </button>

      {filtered.length === 0 ? (
        <div role="status" style={{
          textAlign: "center", padding: "32px 12px",
          color: TOKENS.textMuted, fontSize: 13,
        }}>
          Aucun serveur ne correspond à votre recherche.
        </div>
      ) : (
        <ul role="list" style={{
          listStyle: "none", padding: 0, margin: 0,
          display: "flex", flexDirection: "column", gap: 6,
        }}>
          {filtered.map((s) => (
            <li key={s.id}>
              <ServerCard
                server={s}
                selected={s.id === selectedId}
                favorite={favorites.has(s.id)}
                showFavoriteToggle
                onClick={() => onSelect(s.id)}
                onToggleFavorite={() => onToggleFavorite(s.id)}
              />
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

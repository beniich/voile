import React, { useEffect, useRef } from "react";
import { Globe, Wifi, Lock, Shield, X } from "lucide-react";
import { TOKENS } from "@voile/core/tokens";
import { ToggleSwitch } from "./ToggleSwitch.js";

const SAMPLE_APPS = [
  { id: "browser", name: "Navigateur", icon: Globe },
  { id: "streaming", name: "Streaming vidéo", icon: Wifi },
  { id: "banking", name: "App bancaire", icon: Lock },
  { id: "mail", name: "Client email", icon: Shield },
  { id: "social", name: "Réseaux sociaux", icon: Wifi },
  { id: "cloud", name: "Cloud storage", icon: Globe },
] as const;

interface SplitTunnelingModalProps {
  open: boolean;
  selectedApps: string[];
  onToggle: (id: string) => void;
  onClose: () => void;
}

export function SplitTunnelingModal({
  open,
  selectedApps,
  onToggle,
  onClose,
}: SplitTunnelingModalProps) {
  const closeRef = useRef<HTMLButtonElement>(null);
  const modalRef = useRef<HTMLDivElement>(null);

  // Focus initial sur le bouton de fermeture à l'ouverture
  useEffect(() => {
    if (open && closeRef.current) {
      closeRef.current.focus();
    }
  }, [open]);

  // Fermeture via Escape
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  // Focus trap : Tab cycle entre les éléments focusables
  useEffect(() => {
    if (!open || !modalRef.current) return;
    const modal = modalRef.current;

    const handler = (e: KeyboardEvent) => {
      if (e.key !== "Tab") return;
      const focusables = modal.querySelectorAll<HTMLElement>(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      if (focusables.length === 0) return;

      const first = focusables[0];
      const last = focusables[focusables.length - 1];
      const active = document.activeElement as HTMLElement | null;

      if (e.shiftKey && active === first) {
        e.preventDefault();
        last?.focus();
      } else if (!e.shiftKey && active === last) {
        e.preventDefault();
        first?.focus();
      }
    };

    modal.addEventListener("keydown", handler);
    return () => modal.removeEventListener("keydown", handler);
  }, [open]);

  // Verrouillage du scroll du body pendant que la modal est ouverte
  useEffect(() => {
    if (!open) return;
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prevOverflow;
    };
  }, [open]);

  if (!open) return null;

  const selectedCount = selectedApps.length;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="split-modal-title"
      aria-describedby="split-modal-desc"
      onClick={onClose}
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.6)",
        display: "flex",
        alignItems: "flex-end",
        justifyContent: "center",
        zIndex: 999,
        animation: "voile-fade-in 0.2s ease-out",
      }}
    >
      <div
        ref={modalRef}
        onClick={(e) => e.stopPropagation()}
        style={{
          width: "100%",
          maxWidth: 400,
          background: TOKENS.surface,
          borderTopLeftRadius: 20,
          borderTopRightRadius: 20,
          padding: "20px 20px 28px",
          border: `1px solid ${TOKENS.borderSoft}`,
          borderBottom: "none",
          animation: "voile-slide-up 0.25s ease-out",
        }}
      >
        {/* Header */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            marginBottom: 14,
          }}
        >
          <h3
            id="split-modal-title"
            style={{
              margin: 0,
              fontSize: 16,
              color: TOKENS.textPrimary,
              fontFamily: "'Space Grotesk', sans-serif",
              fontWeight: 600,
            }}
          >
            Split tunneling
          </h3>
          <button
            ref={closeRef}
            onClick={onClose}
            aria-label="Fermer la fenêtre de split tunneling"
            style={{
              background: TOKENS.surfaceElevated,
              border: "none",
              borderRadius: "50%",
              width: 28,
              height: 28,
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <X size={14} color={TOKENS.textSecondary} aria-hidden="true" />
          </button>
        </div>

        {/* Description */}
        <p
          id="split-modal-desc"
          style={{
            fontSize: 12.5,
            color: TOKENS.textMuted,
            margin: "0 0 14px",
            lineHeight: 1.5,
          }}
        >
          Les applications cochées contournent le VPN et utilisent votre
          connexion directe.
          {selectedCount > 0 && (
            <>
              {" "}
              <strong style={{ color: TOKENS.textSecondary }}>
                {selectedCount} application{selectedCount > 1 ? "s" : ""}{" "}
                exclue{selectedCount > 1 ? "s" : ""}.
              </strong>
            </>
          )}
        </p>

        {/* Liste des apps */}
        <ul
          role="list"
          style={{
            listStyle: "none",
            padding: 0,
            margin: 0,
            display: "flex",
            flexDirection: "column",
            gap: 8,
          }}
        >
          {SAMPLE_APPS.map((app) => {
            const Icon = app.icon;
            const isOn = selectedApps.includes(app.id);

            return (
              <li
                key={app.id}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 12,
                  padding: "12px",
                  background: TOKENS.surfaceElevated,
                  borderRadius: 12,
                }}
              >
                <div
                  aria-hidden="true"
                  style={{
                    width: 32,
                    height: 32,
                    borderRadius: 8,
                    background: TOKENS.bg,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    flexShrink: 0,
                  }}
                >
                  <Icon size={15} color={TOKENS.textSecondary} />
                </div>

                <span
                  style={{
                    flex: 1,
                    fontSize: 13.5,
                    color: TOKENS.textPrimary,
                  }}
                >
                  {app.name}
                </span>

                <ToggleSwitch
                  label={`Contourner le VPN pour ${app.name}`}
                  checked={isOn}
                  onChange={() => onToggle(app.id)}
                />
              </li>
            );
          })}
        </ul>

        {/* Note informative */}
        <p
          role="note"
          style={{
            marginTop: 16,
            marginBottom: 0,
            padding: "10px 12px",
            background: TOKENS.connectingDim,
            border: `1px solid ${TOKENS.connecting}`,
            borderRadius: 10,
            fontSize: 11.5,
            color: TOKENS.textSecondary,
            display: "flex",
            alignItems: "flex-start",
            gap: 8,
            lineHeight: 1.5,
          }}
        >
          <span
            aria-hidden="true"
            style={{
              color: TOKENS.connecting,
              fontWeight: 700,
              flexShrink: 0,
            }}
          >
            ⚠
          </span>
          <span>
            Les applications exclues ne sont{" "}
            <strong>pas protégées</strong> par le tunnel chiffré. Utilisez cette
            option uniquement pour les apps qui refusent les IP de Cloudflare.
          </span>
        </p>

        {/* Bouton de fermeture en pied de modal (accessibilité mobile) */}
        <button
          onClick={onClose}
          style={{
            marginTop: 16,
            width: "100%",
            padding: "11px",
            background: TOKENS.secured,
            color: TOKENS.bg,
            border: "none",
            borderRadius: 10,
            fontSize: 14,
            fontWeight: 600,
            cursor: "pointer",
            fontFamily: "inherit",
          }}
        >
          Terminé
        </button>
      </div>
    </div>
  );
}

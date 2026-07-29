import React, { useState } from "react";
import { Sparkles, Check, X, CreditCard } from "lucide-react";
import { TOKENS } from "@voile/core/tokens";
import { loadStripe } from "@stripe/stripe-js";

// Remplacez par votre clé publique Stripe réelle
const stripePromise = loadStripe("pk_test_TYooMQauvdEDq54NiTphI7jx");

interface PremiumModalProps {
  open: boolean;
  onClose: () => void;
  userId?: string;
}

export function PremiumModal({ open, onClose, userId }: PremiumModalProps) {
  const [loading, setLoading] = useState(false);

  if (!open) return null;

  const handleSubscribe = async () => {
    setLoading(true);
    // Logique pour créer une session de checkout côté backend
    // 1. fetch('/api/create-checkout-session', { method: 'POST', body: JSON.stringify({ userId }) })
    // 2. const { sessionId } = await res.json()
    // 3. const stripe = await stripePromise;
    // 4. await stripe.redirectToCheckout({ sessionId })
    
    // Simulation pour le MVP
    setTimeout(() => {
      alert("Redirection vers Stripe Checkout (simulation)...");
      setLoading(false);
      onClose();
    }, 1500);
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="premium-title"
      style={{
        position: "fixed", inset: 0,
        background: "rgba(10, 15, 28, 0.8)",
        backdropFilter: "blur(4px)",
        display: "flex", alignItems: "center", justifyContent: "center",
        zIndex: 100, padding: 20,
      }}
    >
      <div style={{
        background: TOKENS.surface,
        border: `1px solid ${TOKENS.borderSoft}`,
        borderRadius: 24, width: "100%", maxWidth: 400,
        padding: 24, position: "relative",
        boxShadow: `0 24px 48px rgba(0,0,0,0.4), 0 0 0 1px ${TOKENS.securedDim}`,
      }}>
        <button
          onClick={onClose}
          aria-label="Fermer"
          style={{
            position: "absolute", top: 16, right: 16,
            background: "transparent", border: "none",
            color: TOKENS.textMuted, cursor: "pointer",
          }}
        >
          <X size={20} />
        </button>

        <div style={{ textAlign: "center", marginBottom: 24 }}>
          <div style={{
            width: 48, height: 48, borderRadius: "50%",
            background: TOKENS.securedDim, color: TOKENS.secured,
            display: "flex", alignItems: "center", justifyContent: "center",
            margin: "0 auto 16px",
          }}>
            <Sparkles size={24} />
          </div>
          <h2 id="premium-title" style={{
            fontSize: 22, color: TOKENS.textPrimary,
            margin: "0 0 8px", fontFamily: "'Space Grotesk', sans-serif",
          }}>
            Passez à Voile+
          </h2>
          <p style={{ fontSize: 13, color: TOKENS.textSecondary, margin: 0, lineHeight: 1.5 }}>
            Passez au niveau supérieur de la confidentialité avec des fonctionnalités premium conçues pour les power users.
          </p>
        </div>

        <ul style={{ listStyle: "none", padding: 0, margin: "0 0 24px", display: "flex", flexDirection: "column", gap: 12 }}>
          {[
            "Multi-hop (Double VPN) pour un anonymat maximal",
            "IP Dédiée dans le pays de votre choix",
            "Jusqu'à 5 appareils simultanés",
            "Support prioritaire 24/7",
          ].map((feature, i) => (
            <li key={i} style={{ display: "flex", alignItems: "flex-start", gap: 12, fontSize: 13, color: TOKENS.textPrimary }}>
              <div style={{
                width: 18, height: 18, borderRadius: "50%",
                background: TOKENS.secured, color: TOKENS.bg,
                display: "flex", alignItems: "center", justifyContent: "center",
                flexShrink: 0, marginTop: 1,
              }}>
                <Check size={12} strokeWidth={3} />
              </div>
              <span style={{ lineHeight: 1.4 }}>{feature}</span>
            </li>
          ))}
        </ul>

        <div style={{
          background: TOKENS.bg, borderRadius: 12, padding: 16,
          display: "flex", justifyContent: "space-between", alignItems: "center",
          marginBottom: 24, border: `1px solid ${TOKENS.borderSoft}`,
        }}>
          <div>
            <div style={{ fontSize: 12, color: TOKENS.textMuted, marginBottom: 2 }}>Facturation annuelle</div>
            <div style={{ fontSize: 24, color: TOKENS.textPrimary, fontWeight: 700, fontFamily: "'Space Grotesk', sans-serif" }}>
              4,99€ <span style={{ fontSize: 13, color: TOKENS.textMuted, fontWeight: 400 }}>/mois</span>
            </div>
          </div>
          <div style={{ background: TOKENS.securedDim, color: TOKENS.secured, padding: "4px 8px", borderRadius: 8, fontSize: 11, fontWeight: 600 }}>
            -40%
          </div>
        </div>

        <button
          onClick={handleSubscribe}
          disabled={loading}
          style={{
            width: "100%", padding: "14px", borderRadius: 12,
            background: TOKENS.secured, color: TOKENS.bg,
            border: "none", fontSize: 14, fontWeight: 600,
            cursor: loading ? "not-allowed" : "pointer",
            display: "flex", justifyContent: "center", alignItems: "center", gap: 8,
            opacity: loading ? 0.7 : 1,
          }}
        >
          {loading ? (
            <span style={{ animation: "spin 1s linear infinite", display: "inline-block" }}>⏳</span>
          ) : (
            <>
              <CreditCard size={18} />
              Passer à Voile+
            </>
          )}
        </button>
        <div style={{ textAlign: "center", marginTop: 12, fontSize: 11, color: TOKENS.textMuted }}>
          Paiement sécurisé par Stripe. Annulable à tout moment.
        </div>
      </div>
    </div>
  );
}

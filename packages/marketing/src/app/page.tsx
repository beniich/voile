"use client";
import { Shield, ShieldCheck, Zap, Lock, Globe, ArrowRight, Star, Check, Sparkles } from "lucide-react";
import styles from "./page.module.css";

const FEATURES = [
  { icon: Zap, title: "WireGuard natif", desc: "Protocole VPN moderne, 2× plus rapide que OpenVPN avec des handshakes cryptographiques en millisecondes." },
  { icon: ShieldCheck, title: "Aucun log d'activité", desc: "Voile ne stocke ni votre trafic, ni vos requêtes DNS, ni votre IP d'origine. Zéro. Punto." },
  { icon: Globe, title: "Réseau Cloudflare", desc: "300+ points de présence dans le monde. Votre trafic transite via l'edge Cloudflare le plus proche." },
  { icon: Lock, title: "Trust Score™", desc: "Notre algorithme inédit évalue votre niveau de protection en temps réel et détecte les fuites DNS." },
];

const PLANS = [
  {
    name: "Gratuit",
    price: "0€",
    period: "pour toujours",
    features: ["1 appareil", "7 serveurs", "WireGuard", "Trust Score"],
    cta: "Commencer gratuitement",
    href: "https://voile.ricecloud.com",
    highlight: false,
  },
  {
    name: "Voile+",
    price: "4,99€",
    period: "/mois (annuel)",
    badge: "-40%",
    features: ["5 appareils", "Multi-hop (Double VPN)", "IP Dédiée", "Support prioritaire 24/7"],
    cta: "Passer à Voile+",
    href: "https://voile.ricecloud.com",
    highlight: true,
  },
];

export default function HomePage() {
  return (
    <div className={styles.wrapper}>
      {/* Nav */}
      <nav className={styles.nav}>
        <div className={styles.navInner}>
          <div className={styles.logo}>
            <Shield size={16} color="var(--secured)" strokeWidth={2.2} />
            <span className={styles.logoText}>Voile</span>
          </div>
          <div className={styles.navLinks}>
            <a href="#features">Fonctionnalités</a>
            <a href="#pricing">Tarifs</a>
            <a href="https://status.voile.ricecloud.com" target="_blank" rel="noopener">Status</a>
          </div>
          <a href="https://voile.ricecloud.com" className={styles.ctaSmall}>
            Essayer gratuitement
          </a>
        </div>
      </nav>

      {/* Hero */}
      <section className={styles.hero}>
        <div className={styles.sonarRings} aria-hidden="true">
          <span className={styles.ring} style={{ animationDelay: "0s" }} />
          <span className={styles.ring} style={{ animationDelay: "0.7s" }} />
          <span className={styles.ring} style={{ animationDelay: "1.4s" }} />
        </div>
        <div className={styles.heroContent}>
          <div className={styles.badge}>
            <Zap size={12} color="var(--secured)" />
            Propulsé par Cloudflare WARP
          </div>
          <h1 className={styles.heroTitle}>
            La vie privée,<br />
            <span className={styles.heroAccent}>sans compromis.</span>
          </h1>
          <p className={styles.heroSub}>
            Un VPN transparent, gratuit et rapide utilisant WireGuard et l'infrastructure
            mondiale de Cloudflare. Disponible sur Web et Android.
          </p>
          <div className={styles.heroCtas}>
            <a href="https://voile.ricecloud.com" className={styles.ctaPrimary}>
              Commencer gratuitement
              <ArrowRight size={18} />
            </a>
            <a href="#pricing" className={styles.ctaSecondary}>
              Voir les offres
            </a>
          </div>
          <div className={styles.heroMeta}>
            <span><ShieldCheck size={13} color="var(--secured)" /> Aucune carte requise</span>
            <span><Globe size={13} color="var(--text-muted)" /> 7 pays couverts</span>
            <span><Star size={13} color="var(--connecting)" /> Trust Score™ en temps réel</span>
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className={styles.section}>
        <h2 className={styles.sectionTitle}>Pourquoi Voile ?</h2>
        <p className={styles.sectionSub}>Conçu pour les power users qui refusent les compromis.</p>
        <div className={styles.featuresGrid}>
          {FEATURES.map((f, i) => {
            const Icon = f.icon;
            return (
              <div key={i} className={styles.featureCard}>
                <div className={styles.featureIcon}>
                  <Icon size={20} color="var(--secured)" strokeWidth={1.8} />
                </div>
                <h3 className={styles.featureTitle}>{f.title}</h3>
                <p className={styles.featureDesc}>{f.desc}</p>
              </div>
            );
          })}
        </div>
      </section>

      {/* Pricing */}
      <section id="pricing" className={styles.section}>
        <h2 className={styles.sectionTitle}>Tarifs simples</h2>
        <p className={styles.sectionSub}>Commencez gratuitement. Passez à Voile+ quand vous êtes prêt.</p>
        <div className={styles.pricingGrid}>
          {PLANS.map((plan) => (
            <div key={plan.name} className={`${styles.planCard} ${plan.highlight ? styles.planHighlight : ""}`}>
              {plan.highlight && (
                <div className={styles.planBadge}>
                  <Sparkles size={12} /> Populaire {plan.badge}
                </div>
              )}
              <div className={styles.planName}>{plan.name}</div>
              <div className={styles.planPrice}>
                {plan.price}
                <span className={styles.planPeriod}> {plan.period}</span>
              </div>
              <ul className={styles.planFeatures}>
                {plan.features.map((f) => (
                  <li key={f}>
                    <Check size={14} color={plan.highlight ? "var(--secured)" : "var(--text-muted)"} strokeWidth={2.5} />
                    {f}
                  </li>
                ))}
              </ul>
              <a href={plan.href} className={plan.highlight ? styles.ctaPrimary : styles.ctaOutline}>
                {plan.cta}
              </a>
            </div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer className={styles.footer}>
        <div className={styles.footerInner}>
          <div className={styles.logo}>
            <Shield size={14} color="var(--secured)" />
            <span className={styles.logoText}>Voile VPN</span>
          </div>
          <div className={styles.footerLinks}>
            <a href="https://github.com/voile/voile" target="_blank" rel="noopener">GitHub</a>
            <a href="https://voile.ricecloud.com" target="_blank" rel="noopener">App</a>
            <a href="https://status.voile.ricecloud.com" target="_blank" rel="noopener">Status</a>
            <a href="/docs/PLAY_STORE_GUIDE">Docs</a>
          </div>
          <p className={styles.footerCopy}>MIT © Voile Contributors</p>
        </div>
      </footer>
    </div>
  );
}

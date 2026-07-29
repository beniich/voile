# Plan de Réponse aux Incidents de Sécurité — Voile VPN

> **Version** : 1.0 · **Date** : 2026-07-29  
> **Propriétaire** : Équipe Voile · **Révision** : Annuelle

---

## 1. Objectifs

Ce document définit les procédures à suivre en cas d'incident de sécurité affectant les systèmes Voile VPN. Il couvre :
- La détection et le triage
- La notification et l'escalade
- La contention et l'éradication
- La communication envers les utilisateurs
- Le post-mortem et les leçons apprises

---

## 2. Définitions

| Sévérité | Description | Exemples | SLA de réponse |
|:---:|---|---|---|
| **P1 — Critique** | Impact direct sur la confidentialité de données utilisateur | Fuite de tokens, accès non autorisé à la DB | < 1 heure |
| **P2 — Haute** | Indisponibilité du service principal (> 5 min) | Panne Cloudflare, Supabase down | < 4 heures |
| **P3 — Moyenne** | Dégradation partielle, aucun impact sur données | Latence élevée, feature dégradée | < 24 heures |
| **P4 — Basse** | Anomalie mineure, aucun impact utilisateur | Log warning répété | < 72 heures |

---

## 3. Contacts d'Escalade

| Rôle | Contact | Canal |
|---|---|---|
| Responsable Sécurité (RSSI) | `security@voile.app` | Email + Discord |
| DPO (RGPD) | `privacy@voile.app` | Email |
| Supabase Support | support.supabase.com | Ticket Priority |
| Cloudflare Security | `security@cloudflare.com` | Email |
| Stripe Fraud | `security@stripe.com` | Email |

---

## 4. Runbook par Type d'Incident

### 4.1 Fuite de données utilisateur (P1)

```
1. [DÉTECTION] Sentry alerte / rapport utilisateur / monitoring
2. [0-15 min] Confirmer la nature de la fuite
   → Quelles données ? Combien d'utilisateurs ? Depuis quand ?
3. [15-30 min] CONTENTION IMMÉDIATE
   a. Supabase → SQL Editor → Révoquer les tokens compromis :
      UPDATE auth.sessions SET expires_at = now() WHERE ...;
   b. Cloudflare → "Under Attack Mode" si DDoS
   c. Stripe → Bloquer les paiements si compromis
4. [30-60 min] NOTIFICATION
   → Si données personnelles : notifier la CNIL dans 72h (RGPD Art. 33)
   → Email aux utilisateurs impactés (RGPD Art. 34)
5. [1-24h] ÉRADICATION
   → Corriger la vulnérabilité, déployer un patch
   → Rotation des clés API affectées
6. [24-72h] RÉTABLISSEMENT
   → Vérifier que la fuite est bouchée
   → Post-mortem
```

### 4.2 Compromission d'une clé API / Secret (P1)

```
1. Identifier quelle clé est compromise
2. ROTATION IMMÉDIATE :
   # Supabase service role key
   → Supabase Dashboard → Settings → API → Regenerate
   → supabase secrets set SUPABASE_SERVICE_ROLE_KEY=<new>
   → supabase functions deploy (redeploy toutes les functions)

   # Stripe secret key
   → Stripe Dashboard → Developers → API Keys → Roll key

   # Sentry DSN
   → Sentry → Settings → Client Keys → Deactivate old
3. Auditer les logs d'utilisation de l'ancienne clé (48h)
4. Vérifier si des données ont été exfiltrées
```

### 4.3 Indisponibilité du service (P2)

```
1. Vérifier status.cloudflare.com et status.supabase.com
2. Si problème infrastructure tierce → attendre + communication proactive
3. Si problème interne (déploiement cassé) :
   → Cloudflare Pages → Rollback vers le déploiement précédent
   → git revert <commit> && git push origin main
4. Mettre à jour status.voile.ricecloud.com (changer le statut)
5. Tweet / Discord : "Voile VPN rencontre une instabilité..."
```

### 4.4 Vulnérabilité CVE dans une dépendance (P3)

```
1. pnpm audit → identifier la dépendance vulnérable
2. Vérifier si exploitable dans notre contexte (score CVSS > 7 → traiter)
3. pnpm update <package> ou chercher un workaround
4. Tester + déployer
```

---

## 5. Template de Communication Utilisateur (RGPD Art. 34)

```
Objet : Information importante concernant votre compte Voile VPN

Nous vous informons qu'un incident de sécurité a affecté [décrire].

**Ce qui s'est passé** : [Description factuelle]
**Données potentiellement affectées** : [Préciser]
**Ce que nous avons fait** : [Actions correctives]
**Ce que vous pouvez faire** : [Recommandations]

Nous nous excusons sincèrement pour cet incident.
Pour toute question : security@voile.app

L'équipe Voile
```

---

## 6. Post-Mortem Template

Après chaque incident P1/P2, rédiger un post-mortem dans `docs/incidents/YYYY-MM-DD-<titre>.md` dans les 5 jours.

**Structure** :
1. **Résumé** : Quoi, quand, impact
2. **Timeline** : Chronologie précise (UTC)
3. **Root cause** : La vraie cause racine
4. **Facteurs contribuants** : Ce qui a aggravé
5. **Résolution** : Ce qui a résolu
6. **Action items** : Liste de tâches avec responsable + deadline

---

## 7. Notification Réglementaire RGPD

En cas d'incident impliquant des données personnelles :

- **< 72 heures** : Notification à la **CNIL** (notifications.cnil.fr)
- **Dès que possible** : Notification aux utilisateurs impactés si risque élevé
- **Documentation** : Tenir un registre des violations (Article 33§5)

**Critères de notification utilisateurs** (Art. 34) :
- Données sensibles exposées OU
- Risque élevé pour leurs droits et libertés OU
- Données financières compromises

---

*Revu et approuvé par : Équipe Voile · Prochaine révision : 2027-07-29*

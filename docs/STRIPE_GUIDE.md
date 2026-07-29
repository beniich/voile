# Voile+ — Guide d'intégration Stripe

## Architecture

```
Utilisateur → PremiumModal (Web) → create-checkout-session (Edge Function)
    → Stripe Checkout → Paiement → stripe-webhook (Edge Function)
        → profiles.is_premium = true → Fonctionnalités déverrouillées
```

## Prérequis

1. Un compte [Stripe](https://stripe.com)
2. Supabase CLI installé : `npm i -g supabase`
3. Les variables d'environnement suivantes configurées

## 1. Créer un produit Stripe

1. Dashboard Stripe → **Products** → **Add Product**
2. Nom : **Voile+ Premium**
3. Prix : `4,99 €` / mois (facturation annuelle) → copier le `price_xxxxx`

## 2. Variables d'environnement

### Supabase Edge Functions (`.env` local ou secrets Supabase)

```bash
# Clés Stripe (récupérées dans Dashboard → Developers → API keys)
STRIPE_SECRET_KEY=sk_live_xxxxx
STRIPE_WEBHOOK_SECRET=whsec_xxxxx
STRIPE_PRICE_ID=price_xxxxx       # L'ID du prix créé à l'étape 1

# Supabase Service Role (pour mettre à jour les profiles)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_SERVICE_ROLE_KEY=eyJxxx...  # Dans Settings → API
```

Ajouter les secrets sur Supabase :
```bash
supabase secrets set STRIPE_SECRET_KEY=sk_live_xxxxx
supabase secrets set STRIPE_WEBHOOK_SECRET=whsec_xxxxx
supabase secrets set STRIPE_PRICE_ID=price_xxxxx
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=eyJxxx
```

### Web PWA (`.env.local`)

```bash
VITE_STRIPE_PUBLISHABLE_KEY=pk_live_xxxxx
```

## 3. Déployer les Edge Functions

```bash
supabase functions deploy create-checkout-session
supabase functions deploy stripe-webhook
```

## 4. Configurer le Webhook Stripe

1. Dashboard Stripe → **Developers** → **Webhooks** → **Add endpoint**
2. URL : `https://your-project.supabase.co/functions/v1/stripe-webhook`
3. Événements à écouter : `checkout.session.completed`
4. Copier le **Signing secret** (`whsec_xxxxx`) → l'ajouter aux secrets Supabase

## 5. Appliquer la migration SQL

```bash
supabase db push
# Ou manuellement dans l'éditeur SQL de Supabase :
# Copier le contenu de supabase/migrations/20250729_add_premium.sql
```

## 6. Tester en mode test

```bash
# 1. Lancer les fonctions localement
supabase functions serve

# 2. Installer la CLI Stripe pour forwarder les webhooks en local
stripe listen --forward-to localhost:54321/functions/v1/stripe-webhook

# 3. Déclencher un événement de test
stripe trigger checkout.session.completed
```

## Flux complet en production

```
1. User → Paramètres → Voile+ CTA
2. PremiumModal.handleSubscribe() →
   POST /functions/v1/create-checkout-session
3. Edge Function → stripe.checkout.sessions.create()
4. Response → { url: "https://checkout.stripe.com/..." }
5. window.location.href = url  →  Redirect to Stripe Checkout
6. User complète le paiement
7. Stripe POST → /functions/v1/stripe-webhook
8. Vérification de signature HMAC
9. UPDATE profiles SET is_premium = true WHERE id = client_reference_id
10. Redirect → voile.ricecloud.com/?premium=success
11. App détecte le query param → affiche un toast de succès
```

## Features à activer pour les utilisateurs premium

Dans le code, vérifier `profile.is_premium` avant d'activer :
- Multi-hop (2ème serveur relais)
- IP Dédiée (serveur distinct)
- Connexion simultanée sur 5 appareils
- Support prioritaire (lien Discord/email dédié)

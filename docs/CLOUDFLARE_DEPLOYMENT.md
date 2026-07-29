# Guide de déploiement Cloudflare Pages · Voile

Ce guide détaille les étapes nécessaires pour configurer et déployer la PWA Voile (`packages/web`) sur Cloudflare Pages.

---

## 1. Prérequis sur Cloudflare

1. Créez un compte gratuit sur [cloudflare.com](https://www.cloudflare.com).
2. Récupérez votre **ID de compte** (disponible sur le tableau de bord Cloudflare).
3. Créez un **API Token** avec les privilèges suivants :
   - `Account` -> `Cloudflare Pages` -> `Edit`
4. Créez un projet Pages nommé `voile-web` depuis le dashboard Cloudflare (`Workers & Pages` -> `Create` -> `Pages`).

---

## 2. Configuration des secrets sur GitHub

Dans les paramètres de votre dépôt GitHub (`Settings` -> `Secrets and variables` -> `Actions` -> `New repository secret`), ajoutez les secrets suivants :

| Nom du Secret | Description |
|---|---|
| `CLOUDFLARE_API_TOKEN` | Le token API Cloudflare créé ci-dessus. |
| `CLOUDFLARE_ACCOUNT_ID` | Votre ID de compte Cloudflare. |
| `VITE_SENTRY_DSN` | Le DSN de votre projet Sentry `voile-web`. |
| `VITE_SUPABASE_URL` | L'URL de votre instance Supabase. |
| `VITE_SUPABASE_ANON_KEY` | La clé publique (anon) de Supabase. |
| `SENTRY_AUTH_TOKEN` | Token d'authentification Sentry (requis pour l'envoi des source maps). |
| `SENTRY_ORG` | Le slug de votre organisation Sentry. |

---

## 3. Domaine personnalisé & SSL (Optionnel)

Pour lier votre domaine personnalisé (ex: `voile.app`) :
1. Allez sur votre projet Pages dans Cloudflare -> `Custom domains`.
2. Cliquez sur `Set up a custom domain` et saisissez `voile.app`.
3. Laissez Cloudflare gérer automatiquement le certificat SSL et la propagation DNS.

---

## 4. Stratégie de mise en cache & Redirections SPA

- **SPA Fallback** : Le fichier `public/_redirects` force Cloudflare Pages à servir `index.html` pour toutes les routes en cas de rafraîchissement d'une URL de type `/settings` ou `/servers`.
- **En-têtes de sécurité** : Le fichier `public/_headers` configure la politique de sécurité des contenus (CSP), prévient l'iframe-jacking (`X-Frame-Options: DENY`) et active le HSTS pour forcer le HTTPS.

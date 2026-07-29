-- Migration: Ajout du flag is_premium sur profiles
-- Exécuter via: supabase db push

ALTER TABLE public.profiles
  ADD COLUMN IF NOT EXISTS is_premium BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS premium_since TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS stripe_customer_id TEXT;

-- Index pour les requêtes sur les utilisateurs premium
CREATE INDEX IF NOT EXISTS idx_profiles_is_premium ON public.profiles(is_premium);

-- Commentaire de documentation
COMMENT ON COLUMN public.profiles.is_premium IS 'True si l utilisateur a souscrit à Voile+';
COMMENT ON COLUMN public.profiles.premium_since IS 'Date de première souscription à Voile+';
COMMENT ON COLUMN public.profiles.stripe_customer_id IS 'ID client Stripe pour la gestion des abonnements';

# Guide de contribution

## Setup

```bash
pnpm install
pnpm gen:all   # Génère les types Supabase
```

## Workflow

1. **Fork** le repo
2. **Crée une branche** : `git checkout -b feat/ma-feature`
3. **Développe** en respectant l'architecture
4. **Tests** : ajoute des tests pour toute nouvelle logique dans `@voile/core`
5. **Lint** : `pnpm lint`
6. **Commit** : préfixe conventional commits (`feat:`, `fix:`, `docs:`)
7. **PR** : décris le changement, lie l'issue

## Conventions

### Code

- **TypeScript strict** : `noUncheckedIndexedAccess: true`
- **Pas de `any`** sauf interop avec libs externes
- **Composants React** : fonctionnels + hooks, pas de classes
- **Composants Compose** : `@Composable`, stateless quand possible
- **Tests** : AAA pattern, un concept par test

### Commits

```
feat: add multi-hop UI in settings
fix(trust-score): clamp negative latencies
docs: update README with deployment steps
test: add coverage for WARP config parsing
refactor: extract usePersistedState to core
```

### Structure d'un commit

- **Subject** : 50 char max, impératif présent
- **Body** : 72 char par ligne, explique le *pourquoi*
- **Footer** : `Refs: #123` ou `BREAKING CHANGE: ...`

## Pull Requests

- ✅ CI verte (lint + tests sur 3 packages)
- ✅ Au moins 1 approbation
- ✅ Coverage non-régressive
- ✅ Documentation mise à jour si API publique modifiée

## Releases

Versioning sémantique (semver) :

- **MAJOR** : breaking changes (changement d'API publique)
- **MINOR** : nouvelles features rétrocompatibles
- **PATCH** : bug fixes

Releases gérées via `pnpm changeset` (à ajouter).

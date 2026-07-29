import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

import fr from "./locales/fr.json";
import en from "./locales/en.json";
import es from "./locales/es.json";
import de from "./locales/de.json";
import it from "./locales/it.json";

i18n
  .use(LanguageDetector)   // Détecte la langue du navigateur automatiquement
  .use(initReactI18next)
  .init({
    resources: { fr: { translation: fr }, en: { translation: en }, es: { translation: es }, de: { translation: de }, it: { translation: it } },
    fallbackLng: "fr",
    supportedLngs: ["fr", "en", "es", "de", "it"],
    interpolation: { escapeValue: false },
    detection: {
      order: ["navigator", "htmlTag", "localStorage"],
      caches: ["localStorage"],
    },
  });

export default i18n;

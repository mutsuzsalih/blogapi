import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import { localizationAPI } from '../services/api';

// Fallback translation files
import tr from './locales/tr.json';
import en from './locales/en.json';

// Dynamic resource loader
const loadResources = async (language) => {
  try {
    const response = await localizationAPI.getMessages(language);
    return response.data;
  } catch (error) {
    console.warn(`Failed to load ${language} translations from database, using fallback`);
    return language === 'tr' ? tr : en;
  }
};

const resources = {
  en: {
    translation: en
  },
  tr: {
    translation: tr
  }
};

// Initialize with database loader
const initI18n = async () => {
  // Load Turkish translations from database
  try {
    const trTranslations = await loadResources('tr');
    const enTranslations = await loadResources('en');
    
    resources.tr.translation = { ...tr, ...trTranslations };
    resources.en.translation = { ...en, ...enTranslations };
  } catch (error) {
    console.warn('Using fallback translations');
  }

  i18n
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
      resources,
      lng: 'tr', // default language
      fallbackLng: 'tr',
      
      detection: {
        order: ['localStorage', 'navigator', 'htmlTag'],
        caches: ['localStorage']
      },

      interpolation: {
        escapeValue: false, // not needed for react as it escapes by default
      },

      react: {
        useSuspense: false
      }
    });
};

// Initialize i18n
initI18n();

export default i18n; 